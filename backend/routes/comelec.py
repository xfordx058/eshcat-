from datetime import date

from flask import Blueprint, jsonify, request, session

from ..database import get_connection
from .staff import login_required

comelec_bp = Blueprint("comelec", __name__)
COMELEC_DEPARTMENT_ID = 11
QUEUE_STATUSES = {"Waiting", "Serving", "Done", "Cancelled"}


def _comelec_staff_required(view):
    @login_required
    def wrapped(*args, **kwargs):
        if session.get("staff_role") != "Administrator" and session.get("department_id") != COMELEC_DEPARTMENT_ID:
            return jsonify({"error": "COMELEC department access required."}), 403
        return view(*args, **kwargs)

    wrapped.__name__ = view.__name__
    return wrapped


def _settings(conn):
    row = conn.execute("SELECT * FROM comelec_settings WHERE id = 1").fetchone()
    if row is None:
        conn.execute("INSERT INTO comelec_settings (id, queue_room, announcement) VALUES (1, 'COMELEC Room 1', 'Election-day queue is currently inactive.')")
        row = conn.execute("SELECT * FROM comelec_settings WHERE id = 1").fetchone()
    return dict(row)


@comelec_bp.get("/api/comelec/settings")
def public_settings():
    conn = get_connection()
    try:
        settings = _settings(conn)
        return jsonify({
            "active": bool(settings["election_day_active"]),
            "lookup_enabled": bool(settings["public_lookup_enabled"]),
            "election_date": settings.get("election_date"),
            "room": settings["queue_room"],
            "announcement": settings.get("announcement") or "",
        })
    finally:
        conn.close()


@comelec_bp.post("/api/comelec/queue")
def request_queue_number():
    payload = request.get_json(silent=True) or {}
    full_name = (payload.get("full_name") or "").strip()
    email = (payload.get("email") or "").strip()
    precinct = (payload.get("precinct_number") or "").strip()
    purpose = (payload.get("purpose") or "Voter verification").strip()
    if not full_name or not precinct:
        return jsonify({"error": "Full name and precinct number are required."}), 400
    conn = get_connection()
    try:
        settings = _settings(conn)
        if not settings["election_day_active"]:
            return jsonify({"error": "Election-day queue is not active yet."}), 409
        today = date.today().isoformat()
        current = conn.execute("SELECT COALESCE(MAX(present_number), 0) AS last_number FROM comelec_queue WHERE queue_date = ?", (today,)).fetchone()["last_number"]
        number = int(current) + 1
        cursor = conn.execute(
            """INSERT INTO comelec_queue
               (queue_date, present_number, full_name, email, precinct_number, purpose, room)
               VALUES (?, ?, ?, ?, ?, ?, ?)""",
            (today, number, full_name, email or None, precinct, purpose, settings["queue_room"]),
        )
        conn.commit()
        return jsonify({"message": "Present number issued.", "id": cursor.lastrowid, "present_number": number, "room": settings["queue_room"], "queue_date": today, "status": "Waiting"}), 201
    finally:
        conn.close()


@comelec_bp.get("/api/comelec/queue/<int:present_number>")
def lookup_queue_number(present_number):
    conn = get_connection()
    try:
        settings = _settings(conn)
        if not settings["public_lookup_enabled"]:
            return jsonify({"error": "Present-number search is currently disabled."}), 403
        row = conn.execute(
            "SELECT present_number, queue_date, full_name, precinct_number, purpose, room, status FROM comelec_queue WHERE queue_date = ? AND present_number = ?",
            (date.today().isoformat(), present_number),
        ).fetchone()
        if row is None:
            return jsonify({"error": "Present number not found for today."}), 404
        return jsonify(dict(row))
    finally:
        conn.close()


@comelec_bp.get("/api/comelec/dashboard")
@_comelec_staff_required
def dashboard():
    conn = get_connection()
    try:
        settings = _settings(conn)
        rows = conn.execute(
            """SELECT a.id, a.reference_number, a.full_name, a.email, a.mobile, a.address, a.form_data,
                      a.status, a.created_at, a.updated_at, s.name AS service_name
               FROM applications a JOIN services s ON s.id = a.service_id
               WHERE a.department_id = ? ORDER BY a.updated_at DESC, a.created_at DESC""",
            (COMELEC_DEPARTMENT_ID,),
        ).fetchall()
        counts = {status: 0 for status in ("Submitted", "Under Review", "For Verification", "Approved", "Rejected", "Not Found", "Ready for Release", "Completed", "Forwarded")}
        for row in rows:
            if row["status"] in counts:
                counts[row["status"]] += 1
        queue = conn.execute("SELECT * FROM comelec_queue WHERE queue_date = ? ORDER BY present_number DESC", (date.today().isoformat(),)).fetchall()
        return jsonify({"settings": settings, "counts": counts, "applications": [dict(row) for row in rows], "queue": [dict(row) for row in queue]})
    finally:
        conn.close()


@comelec_bp.patch("/api/comelec/settings")
@_comelec_staff_required
def update_settings():
    payload = request.get_json(silent=True) or {}
    active = 1 if payload.get("election_day_active") else 0
    lookup = 1 if payload.get("public_lookup_enabled") else 0
    election_date = (payload.get("election_date") or "").strip() or None
    room = (payload.get("queue_room") or "COMELEC Room 1").strip()
    announcement = (payload.get("announcement") or "").strip()
    conn = get_connection()
    try:
        _settings(conn)
        conn.execute("""UPDATE comelec_settings
                       SET election_day_active = ?, public_lookup_enabled = ?, election_date = ?,
                           queue_room = ?, announcement = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP
                       WHERE id = 1""", (active, lookup, election_date, room, announcement, session.get("staff_id")))
        conn.execute("UPDATE comelec_queue SET room = ?, updated_at = CURRENT_TIMESTAMP WHERE queue_date = ? AND status IN ('Waiting', 'Serving')", (room, date.today().isoformat()))
        conn.commit()
        return jsonify({"message": "COMELEC settings updated."})
    finally:
        conn.close()


@comelec_bp.patch("/api/comelec/queue/<int:queue_id>")
@_comelec_staff_required
def update_queue(queue_id):
    status = (request.get_json(silent=True) or {}).get("status")
    if status not in QUEUE_STATUSES:
        return jsonify({"error": "Invalid queue status."}), 400
    conn = get_connection()
    try:
        cursor = conn.execute("UPDATE comelec_queue SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?", (status, queue_id))
        if cursor.rowcount == 0:
            return jsonify({"error": "Queue entry not found."}), 404
        conn.commit()
        return jsonify({"message": "Queue status updated."})
    finally:
        conn.close()


@comelec_bp.post("/api/comelec/demo-queue")
@_comelec_staff_required
def create_demo_queue():
    """Create 100 clearly-labelled demo voters for presentations and testing."""
    conn = get_connection()
    try:
        settings = _settings(conn)
        queue_date = date.today().isoformat()
        existing_demo = conn.execute(
            "SELECT COUNT(*) AS c FROM comelec_queue WHERE queue_date = ? AND purpose = 'DEMO - Voter verification'",
            (queue_date,),
        ).fetchone()["c"]
        if existing_demo >= 100:
            return jsonify({"message": "The 100 demo queue entries already exist for today.", "created": 0})
        first_number = int(conn.execute("SELECT COALESCE(MAX(present_number), 0) AS n FROM comelec_queue WHERE queue_date = ?", (queue_date,)).fetchone()["n"]) + 1
        first_names = ["Juan", "Maria", "Jose", "Ana", "Ramon", "Liza", "Carlo", "Elena", "Mark", "Grace"]
        last_names = ["Dela Cruz", "Santos", "Reyes", "Garcia", "Mendoza", "Bautista", "Navarro", "Aquino", "Villanueva", "Manalo"]
        rows = []
        for index in range(existing_demo, 100):
            full_name = f"{first_names[index % len(first_names)]} {last_names[index // len(first_names)]} {index + 1:03d}"
            rows.append((queue_date, first_number + index - existing_demo, full_name, f"demo{index + 1:03d}@catarman.example", f"{index % 50 + 1:03d}{chr(65 + index % 3)}", "DEMO - Voter verification", settings["queue_room"], "Waiting"))
        conn.executemany(
            """INSERT INTO comelec_queue
               (queue_date, present_number, full_name, email, precinct_number, purpose, room, status)
               VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
            rows,
        )
        conn.commit()
        return jsonify({"message": "Demo queue entries created.", "created": len(rows), "room": settings["queue_room"]}), 201
    finally:
        conn.close()
