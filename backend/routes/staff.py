import functools
import json

from flask import Blueprint, jsonify, request, session
from werkzeug.security import check_password_hash, generate_password_hash

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


def admin_required(view):
    @functools.wraps(view)
    @login_required
    def wrapped(*args, **kwargs):
        if session.get("staff_role") != "Administrator":
            return jsonify({"error": "Administrator access required."}), 403
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


@staff_bp.get("/api/staff/users")
@admin_required
def list_staff_users():
    conn = get_connection()
    try:
        rows = conn.execute(
            """
            SELECT u.id, u.name, u.email, u.role, u.department_id, u.is_active,
                   d.name AS department_name
            FROM staff_users u
            LEFT JOIN departments d ON d.id = u.department_id
            ORDER BY u.name ASC
            """
        ).fetchall()
        return jsonify([dict(row) for row in rows])
    finally:
        conn.close()


@staff_bp.get("/api/staff/departments")
@admin_required
def list_staff_departments():
    conn = get_connection()
    try:
        rows = conn.execute("SELECT id, name FROM departments ORDER BY name ASC").fetchall()
        return jsonify([dict(row) for row in rows])
    finally:
        conn.close()


@staff_bp.post("/api/staff/users")
@admin_required
def create_staff_user():
    payload = request.get_json(silent=True) or {}
    name = (payload.get("name") or "").strip()
    email = (payload.get("email") or "").strip().lower()
    password = payload.get("password") or ""
    role = (payload.get("role") or "Staff").strip()
    department_id = payload.get("department_id") or None
    if not name or not email or len(password) < 6:
        return jsonify({"error": "Name, email, and a password of at least 6 characters are required."}), 400
    if role not in ("Staff", "Department Head", "Administrator", "Auditor"):
        return jsonify({"error": "Invalid staff role."}), 400

    conn = get_connection()
    try:
        if conn.execute("SELECT id FROM staff_users WHERE email = ?", (email,)).fetchone():
            return jsonify({"error": "A staff account with this email already exists."}), 409
        cursor = conn.execute(
            """
            INSERT INTO staff_users (name, email, password_hash, role, department_id, is_active)
            VALUES (?, ?, ?, ?, ?, 1)
            """,
            (name, email, generate_password_hash(password), role, department_id),
        )
        log_audit(conn, session["staff_id"], "CREATE_STAFF", "staff", cursor.lastrowid, email)
        conn.commit()
        return jsonify({"message": "Staff account created.", "id": cursor.lastrowid}), 201
    finally:
        conn.close()


@staff_bp.patch("/api/staff/users/<int:user_id>")
@admin_required
def update_staff_user(user_id):
    payload = request.get_json(silent=True) or {}
    name = (payload.get("name") or "").strip()
    email = (payload.get("email") or "").strip().lower()
    role = (payload.get("role") or "Staff").strip()
    department_id = payload.get("department_id") or None
    is_active = 1 if payload.get("is_active", True) else 0
    password = payload.get("password") or ""
    if not name or not email:
        return jsonify({"error": "Name and email are required."}), 400
    if role not in ("Staff", "Department Head", "Administrator", "Auditor"):
        return jsonify({"error": "Invalid staff role."}), 400
    if user_id == session.get("staff_id") and not is_active:
        return jsonify({"error": "You cannot deactivate your own account."}), 400

    conn = get_connection()
    try:
        current = conn.execute("SELECT id FROM staff_users WHERE id = ?", (user_id,)).fetchone()
        if current is None:
            return jsonify({"error": "Staff account not found."}), 404
        duplicate = conn.execute(
            "SELECT id FROM staff_users WHERE email = ? AND id <> ?", (email, user_id)
        ).fetchone()
        if duplicate:
            return jsonify({"error": "A staff account with this email already exists."}), 409
        if password:
            conn.execute(
                """
                UPDATE staff_users SET name = ?, email = ?, password_hash = ?, role = ?,
                    department_id = ?, is_active = ? WHERE id = ?
                """,
                (name, email, generate_password_hash(password), role, department_id, is_active, user_id),
            )
        else:
            conn.execute(
                """
                UPDATE staff_users SET name = ?, email = ?, role = ?, department_id = ?,
                    is_active = ? WHERE id = ?
                """,
                (name, email, role, department_id, is_active, user_id),
            )
        log_audit(conn, session["staff_id"], "UPDATE_STAFF", "staff", user_id, email)
        conn.commit()
        return jsonify({"message": "Staff account updated."})
    finally:
        conn.close()


@staff_bp.get("/api/staff/dashboard")
@login_required
def dashboard():
    conn = get_connection()
    try:
        def count_by(sql):
            return conn.execute(sql).fetchone()["c"]

        stats = {
            "total": count_by("SELECT COUNT(*) c FROM applications"),
            "today": count_by("SELECT COUNT(*) c FROM applications WHERE DATE(created_at) = CURRENT_DATE"),
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
        department_filter = request.args.get("department_id", type=int)
        sql = """
            SELECT a.id, a.reference_number, a.full_name, a.status, a.created_at,
                   s.name AS service_name, d.name AS department_name
            FROM applications a
            JOIN services s ON s.id = a.service_id
            JOIN departments d ON d.id = a.department_id
        """
        params = []
        filters = []
        if status_filter:
            filters.append("a.status = ?")
            params.append(status_filter)
        if department_filter:
            filters.append("a.department_id = ?")
            params.append(department_filter)
        if filters:
            sql += " WHERE " + " AND ".join(filters)
        sql += " ORDER BY a.created_at DESC"
        rows = conn.execute(sql, params).fetchall()
        return jsonify([dict(r) for r in rows])
    finally:
        conn.close()


@staff_bp.get("/api/staff/appointments")
@login_required
def list_appointments():
    conn = get_connection()
    try:
        department_filter = request.args.get("department_id", type=int)
        sql = """
            SELECT a.*, s.name AS service_name, d.name AS department_name
            FROM appointments a
            LEFT JOIN services s ON s.id = a.service_id
            LEFT JOIN departments d ON d.id = a.department_id
        """
        params = []
        if department_filter:
            sql += " WHERE a.department_id = ?"
            params.append(department_filter)
        sql += " ORDER BY a.appointment_date ASC, a.appointment_time ASC, a.created_at DESC"
        rows = conn.execute(sql, params).fetchall()
        return jsonify([dict(r) for r in rows])
    finally:
        conn.close()


@staff_bp.get("/api/staff/reports")
@login_required
def list_reports():
    conn = get_connection()
    try:
        department_filter = request.args.get("department_id", type=int)
        sql = "SELECT * FROM community_reports"
        params = []
        if department_filter:
            sql += " WHERE department_id = ?"
            params.append(department_filter)
        sql += " ORDER BY created_at DESC"
        rows = conn.execute(sql, params).fetchall()
        return jsonify([dict(r) for r in rows])
    finally:
        conn.close()


@staff_bp.get("/api/staff/messages")
@login_required
def list_messages():
    conn = get_connection()
    try:
        rows = conn.execute(
            """
            SELECT n.*, a.reference_number
            FROM notifications n
            LEFT JOIN applications a ON a.id = n.application_id
            ORDER BY n.created_at DESC
            """
        ).fetchall()
        return jsonify([dict(r) for r in rows])
    finally:
        conn.close()


@staff_bp.get("/api/staff/activity")
@login_required
def list_activity():
    page = max(request.args.get("page", 1, type=int), 1)
    per_page = 10
    offset = (page - 1) * per_page
    conn = get_connection()
    try:
        total = conn.execute("SELECT COUNT(*) AS c FROM audit_logs").fetchone()["c"]
        rows = conn.execute(
            """
            SELECT l.id, l.action, l.entity_type, l.entity_id, l.details, l.created_at,
                   u.name AS staff_name
            FROM audit_logs l
            LEFT JOIN staff_users u ON u.id = l.staff_id
            ORDER BY l.created_at DESC, l.id DESC
            LIMIT ? OFFSET ?
            """,
            (per_page, offset),
        ).fetchall()
        return jsonify({"items": [dict(row) for row in rows], "page": page, "per_page": per_page, "total": total, "pages": max((total + per_page - 1) // per_page, 1)})
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
            "UPDATE applications SET department_id = ?, status = 'Forwarded', updated_at = CURRENT_TIMESTAMP WHERE id = ?",
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
