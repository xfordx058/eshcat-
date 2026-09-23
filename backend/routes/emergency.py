import functools
import hashlib
import secrets

from flask import Blueprint, jsonify, request, session

from ..database import get_connection, log_audit, make_ref
from ..routes.staff import login_required as staff_login_required

emergency_bp = Blueprint("emergency", __name__)
EMERGENCY_LEVELS = {"Yellow", "Orange", "Red"}
EMERGENCY_CATEGORIES = {"Road accident", "Medical emergency", "Fire", "Flooding", "Other"}


def _record_incident_action(conn, incident_id, staff_id, action):
    existing = conn.execute(
        "SELECT id FROM emergency_incident_actions WHERE incident_id = ? AND staff_id = ? AND action = ?",
        (incident_id, staff_id, action),
    ).fetchone()
    if existing is None:
        conn.execute(
            "INSERT INTO emergency_incident_actions (incident_id, staff_id, action) VALUES (?, ?, ?)",
            (incident_id, staff_id, action),
        )


def drrmo_login_required(view):
    @functools.wraps(view)
    @staff_login_required
    def wrapped(*args, **kwargs):
        conn = get_connection()
        try:
            department = conn.execute(
                "SELECT name FROM departments WHERE id = ?", (session.get("department_id"),)
            ).fetchone()
        finally:
            conn.close()
        if department is None or "disaster risk reduction" not in department["name"].lower():
            return jsonify({"error": "DRRMO staff access required."}), 403
        return view(*args, **kwargs)
    return wrapped


@emergency_bp.get("/api/emergencies")
def list_public_incidents():
    conn = get_connection()
    try:
        rows = conn.execute(
            """SELECT reference_number, severity, category, status, created_at, updated_at
               FROM emergency_incidents ORDER BY created_at DESC LIMIT 30"""
        ).fetchall()
        return jsonify([dict(row) for row in rows])
    finally:
        conn.close()


@emergency_bp.post("/api/emergencies")
def create_emergency_incident():
    payload = request.get_json(silent=True) or {}
    severity = str(payload.get("severity") or "").strip().title()
    category = str(payload.get("category") or "").strip()
    latitude = payload.get("latitude")
    longitude = payload.get("longitude")
    accuracy = payload.get("accuracy_meters")
    location = str(payload.get("location") or "").strip()[:300]
    description = str(payload.get("description") or "").strip()[:1000]
    if severity not in EMERGENCY_LEVELS:
        return jsonify({"error": "Choose Yellow, Orange, or Red incident level."}), 400
    if category not in EMERGENCY_CATEGORIES:
        return jsonify({"error": "Choose a valid emergency category."}), 400
    if latitude is None and longitude is None:
        location = location or "Catarman, Northern Samar (GPS unavailable)"
    else:
        if isinstance(latitude, bool) or isinstance(longitude, bool):
            return jsonify({"error": "A valid device location is required."}), 400
        try:
            latitude, longitude = float(latitude), float(longitude)
        except (TypeError, ValueError):
            return jsonify({"error": "A device location or landmark is required."}), 400
        if not (-90 <= latitude <= 90 and -180 <= longitude <= 180):
            return jsonify({"error": "The reported coordinates are invalid."}), 400
    if accuracy is not None:
        try:
            accuracy = float(accuracy)
        except (TypeError, ValueError):
            return jsonify({"error": "GPS accuracy must be a valid distance in meters."}), 400
        if not (0 <= accuracy <= 100000):
            return jsonify({"error": "GPS accuracy is outside the valid range."}), 400
    reference = make_ref("CAT-EMR")
    followup_token = secrets.token_urlsafe(32)
    conn = get_connection()
    try:
        conn.execute(
            """INSERT INTO emergency_incidents
               (reference_number, severity, category, location, latitude, longitude, accuracy_meters, description, followup_token_hash)
               VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""",
            (reference, severity, category, location, latitude, longitude, accuracy, description,
             hashlib.sha256(followup_token.encode("utf-8")).hexdigest()),
        )
        conn.commit()
        incident = conn.execute("SELECT id FROM emergency_incidents WHERE reference_number = ?", (reference,)).fetchone()
        return jsonify({"id": incident["id"], "reference_number": reference, "followup_token": followup_token, "status": "Open"}), 201
    finally:
        conn.close()


@emergency_bp.patch("/api/emergencies/<int:incident_id>/details")
def add_public_incident_details(incident_id):
    payload = request.get_json(silent=True) or {}
    token = str(payload.get("followup_token") or "")
    if not token:
        return jsonify({"error": "The follow-up authorization is missing."}), 401
    category = str(payload.get("category") or "").strip()
    location = str(payload.get("location") or "").strip()[:300]
    description = str(payload.get("description") or "").strip()[:1000]
    latitude = payload.get("latitude")
    longitude = payload.get("longitude")
    accuracy = payload.get("accuracy_meters")
    if category and category not in EMERGENCY_CATEGORIES:
        return jsonify({"error": "Choose a valid emergency category."}), 400
    if latitude is not None or longitude is not None:
        if isinstance(latitude, bool) or isinstance(longitude, bool):
            return jsonify({"error": "The reported coordinates are invalid."}), 400
        try:
            latitude, longitude = float(latitude), float(longitude)
        except (TypeError, ValueError):
            return jsonify({"error": "Both latitude and longitude are required."}), 400
        if not (-90 <= latitude <= 90 and -180 <= longitude <= 180):
            return jsonify({"error": "The reported coordinates are invalid."}), 400
        if accuracy is not None:
            try:
                accuracy = float(accuracy)
            except (TypeError, ValueError):
                return jsonify({"error": "GPS accuracy must be a valid distance in meters."}), 400
            if not (0 <= accuracy <= 100000):
                return jsonify({"error": "GPS accuracy is outside the valid range."}), 400
    token_hash = hashlib.sha256(token.encode("utf-8")).hexdigest()
    conn = get_connection()
    try:
        incident = conn.execute(
            "SELECT status, followup_token_hash FROM emergency_incidents WHERE id = ?", (incident_id,)
        ).fetchone()
        if incident is None or not secrets.compare_digest(incident["followup_token_hash"] or "", token_hash):
            return jsonify({"error": "This follow-up authorization is invalid."}), 403
        if incident["status"] == "Resolved" and (
            latitude is None or longitude is None or category or location or description
        ):
            return jsonify({"error": "This incident has already been resolved."}), 409
        conn.execute(
            """UPDATE emergency_incidents SET category = COALESCE(NULLIF(?, ''), category),
               location = CASE WHEN ? IS NOT NULL AND ? IS NOT NULL AND ? = ''
                                 AND location = 'Catarman, Northern Samar (GPS unavailable)' THEN NULL
                               ELSE COALESCE(NULLIF(?, ''), location) END,
               latitude = COALESCE(?, latitude), longitude = COALESCE(?, longitude),
               accuracy_meters = COALESCE(?, accuracy_meters),
               description = COALESCE(NULLIF(?, ''), description), updated_at = CURRENT_TIMESTAMP WHERE id = ?""",
            (category, latitude, longitude, location, location, latitude, longitude, accuracy, description, incident_id),
        )
        conn.commit()
        return jsonify({"message": "Emergency details saved."})
    finally:
        conn.close()


@emergency_bp.get("/api/staff/emergencies")
@drrmo_login_required
def list_staff_incidents():
    conn = get_connection()
    try:
        rows = conn.execute(
            """SELECT e.*, responder.name AS responder_name, resolver.name AS resolved_by_name,
                      EXISTS (SELECT 1 FROM emergency_incident_actions ignored
                              WHERE ignored.incident_id = e.id AND ignored.staff_id = ? AND ignored.action = 'Ignored') AS ignored_by_me
               FROM emergency_incidents e
               LEFT JOIN staff_users responder ON responder.id = e.responder_id
               LEFT JOIN staff_users resolver ON resolver.id = e.resolved_by
               WHERE e.status != 'Resolved' ORDER BY
                 CASE e.severity WHEN 'Red' THEN 1 WHEN 'Orange' THEN 2 ELSE 3 END,
                 e.created_at ASC""",
            (session["staff_id"],),
        ).fetchall()
        return jsonify([dict(row) for row in rows])
    finally:
        conn.close()


@emergency_bp.get("/api/staff/emergencies/history")
@drrmo_login_required
def list_staff_emergency_history():
    conn = get_connection()
    try:
        rows = conn.execute(
            """SELECT a.action AS staff_action, a.created_at AS action_at,
                      e.reference_number, e.severity, e.category, e.status,
                      responder.name AS responder_name, resolver.name AS resolved_by_name
               FROM emergency_incident_actions a
               JOIN emergency_incidents e ON e.id = a.incident_id
               LEFT JOIN staff_users responder ON responder.id = e.responder_id
               LEFT JOIN staff_users resolver ON resolver.id = e.resolved_by
               WHERE a.staff_id = ?
               ORDER BY a.created_at DESC, a.id DESC LIMIT 200""",
            (session["staff_id"],),
        ).fetchall()
        return jsonify([dict(row) for row in rows])
    finally:
        conn.close()


@emergency_bp.patch("/api/staff/emergencies/<int:incident_id>")
@drrmo_login_required
def update_incident(incident_id):
    payload = request.get_json(silent=True) or {}
    action = payload.get("action")
    if action not in {"respond", "resolve", "set_severity", "ignore"}:
        return jsonify({"error": "Choose respond, resolve, set_severity, or ignore."}), 400
    staff_id = session["staff_id"]
    conn = get_connection()
    try:
        incident = conn.execute(
            "SELECT id, status, responder_id FROM emergency_incidents WHERE id = ?", (incident_id,)
        ).fetchone()
        if incident is None:
            return jsonify({"error": "Emergency incident not found."}), 404
        if incident["status"] == "Resolved":
            return jsonify({"error": "This emergency has already been resolved."}), 409
        if action == "respond":
            if incident["status"] == "Responding" and incident["responder_id"] != staff_id:
                return jsonify({"error": "Another MDRRMO staff member has already taken this response."}), 409
            conn.execute(
                """UPDATE emergency_incidents SET status = 'Responding', responder_id = COALESCE(responder_id, ?),
                   updated_at = CURRENT_TIMESTAMP WHERE id = ? AND status != 'Resolved'""",
                (staff_id, incident_id),
            )
            _record_incident_action(conn, incident_id, staff_id, "Responded")
            log_audit(conn, staff_id, "TAKE_EMERGENCY", "emergency_incident", incident_id)
        elif action == "resolve":
            conn.execute(
                """UPDATE emergency_incidents SET status = 'Resolved',
                   responder_id = COALESCE(responder_id, ?), resolved_by = ?,
                   updated_at = CURRENT_TIMESTAMP, resolved_at = CURRENT_TIMESTAMP
                   WHERE id = ? AND status != 'Resolved'""",
                (staff_id, staff_id, incident_id),
            )
            _record_incident_action(conn, incident_id, staff_id, "Resolved")
            log_audit(conn, staff_id, "RESOLVE_EMERGENCY", "emergency_incident", incident_id)
        elif action == "ignore":
            _record_incident_action(conn, incident_id, staff_id, "Ignored")
            log_audit(conn, staff_id, "IGNORE_EMERGENCY", "emergency_incident", incident_id)
        else:
            severity = str(payload.get("severity") or "").strip().title()
            if severity not in EMERGENCY_LEVELS:
                return jsonify({"error": "Choose Yellow, Orange, or Red."}), 400
            conn.execute(
                "UPDATE emergency_incidents SET severity = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND status != 'Resolved'",
                (severity, incident_id),
            )
            log_audit(conn, staff_id, "UPDATE_EMERGENCY_LEVEL", "emergency_incident", incident_id, severity)
        conn.commit()
        row = conn.execute(
            """SELECT e.status, e.severity, responder.name AS responder_name, resolver.name AS resolved_by_name
               FROM emergency_incidents e LEFT JOIN staff_users responder ON responder.id = e.responder_id
               LEFT JOIN staff_users resolver ON resolver.id = e.resolved_by WHERE e.id = ?""",
            (incident_id,),
        ).fetchone()
        return jsonify(dict(row))
    finally:
        conn.close()


@emergency_bp.get("/api/emergency-numbers")
def list_emergency_numbers():
    conn = get_connection()
    try:
        rows = conn.execute(
            "SELECT id, label, value FROM emergency_numbers ORDER BY sort_order ASC, id ASC"
        ).fetchall()
        return jsonify([dict(r) for r in rows])
    finally:
        conn.close()


@emergency_bp.put("/api/staff/emergency-numbers")
@staff_login_required
def update_emergency_numbers():
    payload = request.get_json(silent=True) or {}
    items = payload.get("items")
    if not isinstance(items, list) or not items:
        return jsonify({"error": "Provide a non-empty list of items."}), 400

    cleaned = []
    for item in items:
        label = (item.get("label") or "").strip()
        value = (item.get("value") or "").strip()
        if not label or not value:
            return jsonify({"error": "Every number needs both a label and a value."}), 400
        cleaned.append((label, value))

    conn = get_connection()
    try:
        with conn:
            conn.execute("DELETE FROM emergency_numbers")
            conn.executemany(
                "INSERT INTO emergency_numbers (label, value, sort_order, updated_by) VALUES (?, ?, ?, ?)",
                [(label, value, i, session["staff_id"]) for i, (label, value) in enumerate(cleaned)],
            )
            log_audit(
                conn,
                session["staff_id"],
                "UPDATE",
                "emergency_numbers",
                None,
                f"Saved {len(cleaned)} number(s)",
            )
        rows = conn.execute(
            "SELECT id, label, value FROM emergency_numbers ORDER BY sort_order ASC, id ASC"
        ).fetchall()
        return jsonify({"items": [dict(r) for r in rows]})
    finally:
        conn.close()
