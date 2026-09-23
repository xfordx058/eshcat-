from flask import Blueprint, jsonify, request

from ..database import get_connection
from ..services.application_service import get_application_by_reference

tracking_bp = Blueprint("tracking", __name__)


@tracking_bp.route("/api/track/<reference>", methods=["GET"])
def track(reference):
    result = get_application_by_reference(reference)
    if result is not None:
        return jsonify(result)

    conn = get_connection()
    try:
        row = conn.execute(
            """
            SELECT a.*, d.name AS department_name, s.name AS service_name
            FROM appointments a
            LEFT JOIN departments d ON d.id = a.department_id
            LEFT JOIN services s ON s.id = a.service_id
            WHERE a.reference_number = ?
            """,
            (reference.strip().upper(),),
        ).fetchone()
        if row is None:
            return jsonify({"error": "No request found for that reference number."}), 404
        history = conn.execute(
            "SELECT old_status, new_status, remarks, created_at FROM appointment_history WHERE appointment_id = ? ORDER BY created_at ASC, id ASC",
            (row["id"],),
        ).fetchall()
        return jsonify({
            "reference_number": row["reference_number"],
            "service": row["service_name"] or "Office Appointment",
            "department": row["department_name"] or "Municipal Office",
            "status": row["status"],
            "updated_at": row["updated_at"],
            "created_at": row["created_at"],
            "request_type": "appointment",
            "appointment_date": row["appointment_date"],
            "appointment_time": row["appointment_time"],
            "timeline": [dict(item) for item in history],
        })
    finally:
        conn.close()
