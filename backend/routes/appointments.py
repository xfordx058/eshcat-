from flask import Blueprint, jsonify, request

from ..database import get_connection
from ..services.reference_service import generate_reference

appointments_bp = Blueprint("appointments", __name__)


@appointments_bp.route("/api/appointments", methods=["POST"])
def request_appointment():
    payload = request.get_json(silent=True) or {}
    department_id = payload.get("department_id")
    service_id = payload.get("service_id")
    date = payload.get("date")
    time = payload.get("time")
    full_name = (payload.get("full_name") or "").strip()
    email = (payload.get("email") or "").strip()
    mobile = payload.get("mobile") or ""

    if not full_name or not email or not date or not time:
        return jsonify({"error": "Name, email, date and time are required."}), 400

    conn = get_connection()
    try:
        reference = generate_reference("CAT-APT")
        conn.execute(
            """
            INSERT INTO appointments
                (reference_number, department_id, service_id, appointment_date, appointment_time, full_name, email, mobile)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (reference, department_id, service_id, date, time, full_name, email, mobile),
        )
        conn.commit()
        return jsonify(
            {
                "message": "Appointment requested.",
                "reference_number": reference,
            }
        ), 201
    finally:
        conn.close()