import functools
import json

from flask import Blueprint, jsonify, request, session
from werkzeug.security import check_password_hash

from ..database import get_connection, log_audit
from ..services.application_service import update_status

staff_bp = Blueprint("staff", __name__)


def login_required(view):
    @functools.wraps(view)
    def wrapped(*args, **kwargs):
        if session.get("staff_id") is None:
            return jsonify({"error": "Authentication required."}), 401
        return view(*args, **kwargs)

    return wrapped


@staff_bp.post("/api/staff/login")
def login():
    payload = request.get_json(silent=True) or {}
    email = (payload.get("email") or "").strip().lower()
    password = payload.get("password") or ""

    conn = get_connection()
    try:
        user = conn.execute(
            "SELECT * FROM staff_users WHERE email = ? AND is_active = 1", (email,)
        ).fetchone()
        if user is None or not check_password_hash(user["password_hash"], password):
            return jsonify({"error": "Invalid email or password."}), 401

        session.clear()
        session["staff_id"] = user["id"]
        session["staff_name"] = user["name"]
        session["staff_role"] = user["role"]
        session["department_id"] = user["department_id"]
        session.permanent = True

        log_audit(conn, user["id"], "LOGIN", "staff", user["id"])
        conn.commit()
        return jsonify(
            {
                "id": user["id"],
                "name": user["name"],
                "email": user["email"],
                "role": user["role"],
                "department_id": user["department_id"],
            }
        )
    finally:
        conn.close()


@staff_bp.post("/api/staff/logout")
@login_required
def logout():
    conn = get_connection()
    try:
        log_audit(conn, session["staff_id"], "LOGOUT", "staff", session["staff_id"])
        conn.commit()
    finally:
        conn.close()
    session.clear()
    return jsonify({"message": "Logged out."})


@staff_bp.get("/api/staff/me")
@login_required
def me():
    return jsonify(
        {
            "id": session["staff_id"],
            "name": session["staff_name"],
            "role": session["staff_role"],
            "department_id": session["department_id"],
        }
    )


@staff_bp.get("/api/staff/dashboard")
@login_required
def dashboard():
    conn = get_connection()
    try:
        def count_by(sql):
            return conn.execute(sql).fetchone()["c"]

        stats = {
            "total": count_by("SELECT COUNT(*) c FROM applications"),
            "today": count_by("SELECT COUNT(*) c FROM applications WHERE date(created_at) = date('now')"),
            "Pending": count_by("SELECT COUNT(*) c FROM applications WHERE status = 'Submitted'"),
            "Received": count_by("SELECT COUNT(*) c FROM applications WHERE status = 'Received'"),
            "Under Review": count_by("SELECT COUNT(*) c FROM applications WHERE status = 'Under Review'"),
            "Approved": count_by("SELECT COUNT(*) c FROM applications WHERE status = 'Approved'"),
            "Rejected": count_by("SELECT COUNT(*) c FROM applications WHERE status = 'Rejected'"),
            "Completed": count_by("SELECT COUNT(*) c FROM applications WHERE status = 'Completed'"),
            "appointments": count_by("SELECT COUNT(*) c FROM appointments WHERE status = 'Pending'"),
            "reports": count_by("SELECT COUNT(*) c FROM community_reports WHERE status = 'Open'"),
        }
        return jsonify(stats)
    finally:
        conn.close()


@staff_bp.get("/api/staff/applications")
@login_required
def list_applications():
    conn = get_connection()
    try:
        status_filter = request.args.get("status")
        sql = """
            SELECT a.id, a.reference_number, a.full_name, a.status, a.created_at,
                   s.name AS service_name, d.name AS department_name
            FROM applications a
            JOIN services s ON s.id = a.service_id
            JOIN departments d ON d.id = a.department_id
        """
        params = []
        if status_filter:
            sql += " WHERE a.status = ?"
            params.append(status_filter)
        sql += " ORDER BY a.created_at DESC"
        rows = conn.execute(sql, params).fetchall()
        return jsonify([dict(r) for r in rows])
    finally:
        conn.close()


@staff_bp.get("/api/staff/applications/<int:app_id>")
@login_required
def get_application(app_id):
    conn = get_connection()
    try:
        row = conn.execute(
            """
            SELECT a.*, s.name AS service_name, s.estimated_processing, d.name AS department_name
            FROM applications a
            JOIN services s ON s.id = a.service_id
            JOIN departments d ON d.id = a.department_id
            WHERE a.id = ?
            """,
            (app_id,),
        ).fetchone()
        if row is None:
            return jsonify({"error": "Application not found."}), 404
        history = conn.execute(
            """
            SELECT h.*, su.name AS staff_name
            FROM application_history h
            LEFT JOIN staff_users su ON su.id = h.staff_id
            WHERE h.application_id = ?
            ORDER BY h.created_at DESC
            """,
            (app_id,),
        ).fetchall()
        result = dict(row)
        result["form_data"] = json.loads(row["form_data"] or "{}")
        result["history"] = [dict(h) for h in history]
        log_audit(conn, session["staff_id"], "VIEW_APPLICATION", "application", app_id)
        conn.commit()
        return jsonify(result)
    finally:
        conn.close()


@staff_bp.patch("/api/staff/applications/<int:app_id>/status")
@login_required
def patch_status(app_id):
    payload = request.get_json(silent=True) or {}
    new_status = payload.get("status")
    remarks = payload.get("remarks") or ""
    try:
        ok = update_status(app_id, session["staff_id"], new_status, remarks)
    except ValueError as exc:
        return jsonify({"error": str(exc)}), 400
    if not ok:
        return jsonify({"error": "Application not found."}), 404
    conn = get_connection()
    try:
        log_audit(conn, session["staff_id"], "UPDATE_STATUS", "application", app_id, new_status)
        conn.commit()
    finally:
        conn.close()
    return jsonify({"message": "Status updated.", "status": new_status})


@staff_bp.post("/api/staff/applications/<int:app_id>/forward")
@login_required
def forward_application(app_id):
    payload = request.get_json(silent=True) or {}
    department_id = payload.get("department_id")
    remarks = payload.get("remarks") or ""
    if not department_id:
        return jsonify({"error": "Destination department is required."}), 400
    conn = get_connection()
    try:
        row = conn.execute("SELECT id FROM applications WHERE id = ?", (app_id,)).fetchone()
        if row is None:
            return jsonify({"error": "Application not found."}), 404
        current = conn.execute(
            "SELECT status FROM applications WHERE id = ?", (app_id,)
        ).fetchone()["status"]
        conn.execute(
            "UPDATE applications SET department_id = ?, status = 'Forwarded', updated_at = datetime('now') WHERE id = ?",
            (department_id, app_id),
        )
        conn.execute(
            "INSERT INTO application_history (application_id, staff_id, old_status, new_status, remarks) "
            "VALUES (?, ?, ?, 'Forwarded', ?)",
            (app_id, session["staff_id"], current, remarks or f"Forwarded to department {department_id}"),
        )
        log_audit(
            conn, session["staff_id"], "FORWARD_APPLICATION", "application", app_id,
            f"to department {department_id}",
        )
        conn.commit()
        return jsonify({"message": "Application forwarded."})
    finally:
        conn.close()