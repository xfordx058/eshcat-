import functools

from flask import Blueprint, jsonify, request, session
from werkzeug.security import check_password_hash, generate_password_hash

from ..database import get_connection, log_audit
from ..routes.staff import login_required as staff_login_required
from ..services.reference_service import generate_reference

civil_bp = Blueprint("civil", __name__)

APPT_STATUSES = {"Pending", "Confirmed", "Completed", "Cancelled"}
REQ_STATUSES = {"Open", "In Progress", "Resolved"}

_APPT_QUERY = """
    SELECT a.id, a.reference_number, a.department_id, d.name AS office,
           a.appointment_date AS date, a.appointment_time AS time,
           a.full_name, a.email, a.mobile, a.status, a.remarks,
           a.created_at, a.updated_at,
           u.full_name AS submitted_by
    FROM appointments a
    LEFT JOIN departments d ON d.id = a.department_id
    LEFT JOIN civil_users u ON u.id = a.user_id
"""

_REP_QUERY = """
    SELECT r.id, r.reference_number, r.department_id, r.category, r.location, r.description,
           r.name, r.email, r.status, r.remarks, r.created_at, r.updated_at,
           u.full_name AS submitted_by
    FROM community_reports r
    LEFT JOIN civil_users u ON u.id = r.user_id
"""


def civil_login_required(view):
    @functools.wraps(view)
    def wrapped(*args, **kwargs):
        if session.get("civil_user_id") is None:
            return jsonify({"error": "Authentication required."}), 401
        return view(*args, **kwargs)

    return wrapped


def _load_user(conn, user_id):
    row = conn.execute(
        "SELECT id, username, full_name, email FROM civil_users WHERE id = ?",
        (user_id,),
    ).fetchone()
    return dict(row) if row else None


# ---------------------------------------------------------------------------
# Resident account
# ---------------------------------------------------------------------------


@civil_bp.post("/api/civil/register")
def register():
    payload = request.get_json(silent=True) or {}
    username = (payload.get("username") or "").strip().lower()
    full_name = (payload.get("full_name") or "").strip()
    email = (payload.get("email") or "").strip()
    password = payload.get("password") or ""

    if not username or not full_name or not password:
        return jsonify({"error": "Username, full name and password are required."}), 400
    if len(password) < 6:
        return jsonify({"error": "Password must be at least 6 characters."}), 400

    conn = get_connection()
    try:
        exists = conn.execute(
            "SELECT 1 FROM civil_users WHERE username = ?", (username,)
        ).fetchone()
        if exists:
            return jsonify({"error": "That username is already taken."}), 409
        conn.execute(
            "INSERT INTO civil_users (username, password_hash, full_name, email) VALUES (?, ?, ?, ?)",
            (username, generate_password_hash(password), full_name, email or None),
        )
        user = _load_user(conn, conn.execute(
            "SELECT id FROM civil_users WHERE username = ?", (username,)
        ).fetchone()["id"])
        conn.commit()
        session.clear()
        session["civil_user_id"] = user["id"]
        session["civil_user_name"] = user["full_name"]
        session["civil_user_email"] = user["email"]
        session.permanent = True
        return jsonify({"message": "Registered. Welcome!", "user": user}), 201
    finally:
        conn.close()


@civil_bp.post("/api/civil/login")
def login():
    payload = request.get_json(silent=True) or {}
    username = (payload.get("username") or "").strip().lower()
    password = payload.get("password") or ""

    conn = get_connection()
    try:
        user = conn.execute(
            "SELECT * FROM civil_users WHERE username = ?", (username,)
        ).fetchone()
        if user is None or not check_password_hash(user["password_hash"], password):
            return jsonify({"error": "Invalid username or password."}), 401
        user = _load_user(conn, user["id"])
        session.clear()
        session["civil_user_id"] = user["id"]
        session["civil_user_name"] = user["full_name"]
        session["civil_user_email"] = user["email"]
        session.permanent = True
        return jsonify({"message": "Signed in.", "user": user})
    finally:
        conn.close()


@civil_bp.post("/api/civil/logout")
@civil_login_required
def logout():
    session.pop("civil_user_id", None)
    session.pop("civil_user_name", None)
    session.pop("civil_user_email", None)
    return jsonify({"message": "Signed out."})


@civil_bp.get("/api/civil/me")
def me():
    if session.get("civil_user_id") is None:
        return jsonify({"error": "Not signed in."}), 401
    conn = get_connection()
    try:
        user = _load_user(conn, session["civil_user_id"])
        if user is None:
            return jsonify({"error": "Not signed in."}), 401
        return jsonify(user)
    finally:
        conn.close()


# ---------------------------------------------------------------------------
# Resident transactions (forwarded to the civil service desk)
# ---------------------------------------------------------------------------


@civil_bp.post("/api/civil/appointments")
@civil_login_required
def submit_appointment():
    payload = request.get_json(silent=True) or {}
    department_id = payload.get("department_id")
    date = payload.get("date")
    time = payload.get("time")
    full_name = (payload.get("full_name") or "").strip()
    email = (payload.get("email") or "").strip()
    mobile = payload.get("mobile") or ""

    if not department_id or not date or not time or not full_name or not email:
        return jsonify({"error": "Office, preferred date, time, name and email are required."}), 400

    conn = get_connection()
    try:
        reference = generate_reference("CAT-APT")
        conn.execute(
            """
            INSERT INTO appointments
                (reference_number, department_id, appointment_date, appointment_time,
                 full_name, email, mobile, status, user_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'Pending', ?)
            """,
            (reference, department_id, date, time, full_name, email, mobile, session["civil_user_id"]),
        )
        conn.commit()
        row = conn.execute(_APPT_QUERY + " WHERE a.reference_number = ?", (reference,)).fetchone()
        out = dict(row)
        out["type"] = "appointment"
        return jsonify(
            {"message": "Appointment request forwarded to the Civil Service desk.", "reference_number": reference, "item": out}
        ), 201
    finally:
        conn.close()


@civil_bp.post("/api/civil/reports")
@civil_login_required
def submit_report():
    payload = request.get_json(silent=True) or {}
    category = (payload.get("category") or "").strip()
    location = (payload.get("location") or "").strip()
    description = (payload.get("description") or "").strip()

    if not category or not description:
        return jsonify({"error": "Category and description are required."}), 400

    conn = get_connection()
    try:
        reference = generate_reference("CAT-REP")
        conn.execute(
            """
            INSERT INTO community_reports
                (reference_number, department_id, category, location, description, name, email, status, user_id)
            VALUES (?, 1, ?, ?, ?, ?, ?, 'Open', ?)
            """,
            (
                reference,
                category,
                location,
                description,
                session["civil_user_name"],
                session.get("civil_user_email") or "",
                session["civil_user_id"],
            ),
        )
        conn.commit()
        row = conn.execute(_REP_QUERY + " WHERE r.reference_number = ?", (reference,)).fetchone()
        out = dict(row)
        out["type"] = "request"
        return jsonify(
            {"message": "Request forwarded to the Civil Service desk.", "reference_number": reference, "item": out}
        ), 201
    finally:
        conn.close()


@civil_bp.get("/api/civil/transactions")
@civil_login_required
def my_transactions():
    conn = get_connection()
    try:
        uid = session["civil_user_id"]
        appts = conn.execute(
            _APPT_QUERY + " WHERE a.user_id = ? ORDER BY a.created_at DESC", (uid,)
        ).fetchall()
        reps = conn.execute(
            _REP_QUERY + " WHERE r.user_id = ? ORDER BY r.created_at DESC", (uid,)
        ).fetchall()
        items = []
        for a in appts:
            d = dict(a)
            d["type"] = "appointment"
            items.append(d)
        for r in reps:
            d = dict(r)
            d["type"] = "request"
            items.append(d)
        items.sort(key=lambda x: x.get("created_at") or "", reverse=True)
        return jsonify(items)
    finally:
        conn.close()


@civil_bp.get("/api/civil/stats")
@civil_login_required
def my_stats():
    conn = get_connection()
    try:
        uid = session["civil_user_id"]

        def c(sql):
            return conn.execute(sql, (uid,)).fetchone()[0]

        return jsonify(
            {
                "appointments": c("SELECT COUNT(*) FROM appointments WHERE user_id = ?"),
                "appointments_pending": c("SELECT COUNT(*) FROM appointments WHERE user_id = ? AND status = 'Pending'"),
                "appointments_confirmed": c("SELECT COUNT(*) FROM appointments WHERE user_id = ? AND status = 'Confirmed'"),
                "requests": c("SELECT COUNT(*) FROM community_reports WHERE user_id = ?"),
                "requests_open": c("SELECT COUNT(*) FROM community_reports WHERE user_id = ? AND status <> 'Resolved'"),
                "requests_resolved": c("SELECT COUNT(*) FROM community_reports WHERE user_id = ? AND status = 'Resolved'"),
            }
        )
    finally:
        conn.close()


# ---------------------------------------------------------------------------
# Civil service desk (staff)
# ---------------------------------------------------------------------------


@civil_bp.get("/api/civil/admin/stats")
@staff_login_required
def admin_stats():
    conn = get_connection()
    try:
        scoped_department = session.get("department_id") if session.get("staff_role") != "Administrator" else None

        def c(table, sql):
            params = (scoped_department,) if scoped_department else ()
            return conn.execute("SELECT COUNT(*) FROM " + table + sql, params).fetchone()[0]

        appt_scope = " WHERE department_id = ? AND " if scoped_department else " WHERE "
        report_scope = " WHERE department_id = ? AND " if scoped_department else " WHERE "
        appt_all = " WHERE department_id = ?" if scoped_department else ""
        report_all = " WHERE department_id = ?" if scoped_department else ""

        return jsonify(
            {
                "appointments": c("appointments", appt_all),
                "appointments_pending": c("appointments", appt_scope + "status = 'Pending'"),
                "appointments_today": c("appointments", appt_scope + "DATE(appointment_date) = CURRENT_DATE AND status <> 'Cancelled'"),
                "appointments_confirmed": c("appointments", appt_scope + "status = 'Confirmed'"),
                "requests": c("community_reports", report_all),
                "requests_open": c("community_reports", report_scope + "status IN ('Open', 'In Progress')"),
                "requests_resolved": c("community_reports", report_scope + "status = 'Resolved'"),
            }
        )
    finally:
        conn.close()


@civil_bp.get("/api/civil/admin/transactions")
@staff_login_required
def admin_transactions():
    t = request.args.get("type")
    status = request.args.get("status")
    conn = get_connection()
    try:
        scoped_department = session.get("department_id") if session.get("staff_role") != "Administrator" else None
        items = []
        if t in (None, "", "appointment"):
            sql = _APPT_QUERY
            params = []
            if status:
                sql += " WHERE a.status = ?"
                params.append(status)
            if scoped_department:
                sql += " WHERE " if " WHERE " not in sql else " AND "
                sql += "a.department_id = ?"
                params.append(scoped_department)
            sql += " ORDER BY a.created_at DESC"
            for a in conn.execute(sql, params).fetchall():
                d = dict(a)
                d["type"] = "appointment"
                items.append(d)
        if t in (None, "", "request"):
            sql = _REP_QUERY
            params = []
            if status:
                sql += " WHERE r.status = ?"
                params.append(status)
            if scoped_department:
                sql += " WHERE " if " WHERE " not in sql else " AND "
                sql += "r.department_id = ?"
                params.append(scoped_department)
            sql += " ORDER BY r.created_at DESC"
            for r in conn.execute(sql, params).fetchall():
                d = dict(r)
                d["type"] = "request"
                items.append(d)
        items.sort(key=lambda x: x.get("created_at") or "", reverse=True)
        return jsonify(items)
    finally:
        conn.close()


@civil_bp.patch("/api/civil/admin/appointments/<int:appt_id>")
@staff_login_required
def update_appointment(appt_id):
    payload = request.get_json(silent=True) or {}
    status = (payload.get("status") or "").strip()
    remarks = (payload.get("remarks") or "").strip()
    if status not in APPT_STATUSES:
        return jsonify({"error": "Invalid appointment status. Use Pending, Confirmed, Completed or Cancelled."}), 400
    conn = get_connection()
    try:
        current = conn.execute("SELECT status, department_id FROM appointments WHERE id = ?", (appt_id,)).fetchone()
        if current is None:
            return jsonify({"error": "Appointment not found."}), 404
        if session.get("staff_role") != "Administrator" and current["department_id"] != session.get("department_id"):
            return jsonify({"error": "This appointment belongs to another department."}), 403
        conn.execute(
            "UPDATE appointments SET status = ?, remarks = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
            (status, remarks, appt_id),
        )
        row = conn.execute(_APPT_QUERY + " WHERE a.id = ?", (appt_id,)).fetchone()
        conn.commit()
        out = dict(row)
        out["type"] = "appointment"
        return jsonify({"message": "Appointment updated.", "item": out})
    finally:
        conn.close()


@civil_bp.patch("/api/civil/admin/reports/<int:report_id>")
@staff_login_required
def update_report(report_id):
    payload = request.get_json(silent=True) or {}
    status = (payload.get("status") or "").strip()
    remarks = (payload.get("remarks") or "").strip()
    if status not in REQ_STATUSES:
        return jsonify({"error": "Invalid request status. Use Open, In Progress or Resolved."}), 400
    conn = get_connection()
    try:
        current = conn.execute("SELECT id, department_id FROM community_reports WHERE id = ?", (report_id,)).fetchone()
        if current is None:
            return jsonify({"error": "Request not found."}), 404
        if session.get("staff_role") != "Administrator" and current["department_id"] != session.get("department_id"):
            return jsonify({"error": "This request belongs to another department."}), 403
        conn.execute(
            "UPDATE community_reports SET status = ?, remarks = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
            (status, remarks, report_id),
        )
        row = conn.execute(_REP_QUERY + " WHERE r.id = ?", (report_id,)).fetchone()
        conn.commit()
        out = dict(row)
        out["type"] = "request"
        return jsonify({"message": "Request updated.", "item": out})
    finally:
        conn.close()


@civil_bp.post("/api/civil/admin/appointments/<int:appt_id>/forward")
@staff_login_required
def forward_appointment(appt_id):
    return _forward_civil_record("appointments", appt_id)


@civil_bp.post("/api/civil/admin/reports/<int:report_id>/forward")
@staff_login_required
def forward_report(report_id):
    return _forward_civil_record("community_reports", report_id)


def _forward_civil_record(table, record_id):
    payload = request.get_json(silent=True) or {}
    department_id = payload.get("department_id")
    remarks = (payload.get("remarks") or "").strip()
    if not department_id:
        return jsonify({"error": "Destination department is required."}), 400
    if table not in ("appointments", "community_reports"):
        return jsonify({"error": "Invalid record type."}), 400
    conn = get_connection()
    try:
        row = conn.execute(f"SELECT id, department_id FROM {table} WHERE id = ?", (record_id,)).fetchone()
        if row is None:
            return jsonify({"error": "Record not found."}), 404
        if session.get("staff_role") != "Administrator" and row["department_id"] != session.get("department_id"):
            return jsonify({"error": "This record belongs to another department."}), 403
        conn.execute(
            f"UPDATE {table} SET department_id = ?, remarks = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
            (department_id, remarks or "Forwarded to another department.", record_id),
        )
        log_audit(
            conn,
            session["staff_id"],
            "FORWARD_CIVIL_RECORD",
            table,
            record_id,
            f"to department {department_id}",
        )
        conn.commit()
        return jsonify({"message": "Record forwarded successfully."})
    finally:
        conn.close()
