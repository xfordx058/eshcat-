from flask import Blueprint, jsonify, request

from ..database import get_connection
from ..services.reference_service import generate_reference

reports_bp = Blueprint("reports", __name__)


@reports_bp.route("/api/reports", methods=["POST"])
def submit_report():
    payload = request.get_json(silent=True) or {}
    category = (payload.get("category") or "").strip()
    location = (payload.get("location") or "").strip()
    description = (payload.get("description") or "").strip()
    name = payload.get("name") or ""
    email = payload.get("email") or ""

    if not category or not description:
        return jsonify({"error": "Category and description are required."}), 400

    conn = get_connection()
    try:
        reference = generate_reference("CAT-REP")
        conn.execute(
            """
            INSERT INTO community_reports (reference_number, category, location, description, name, email)
            VALUES (?, ?, ?, ?, ?, ?)
            """,
            (reference, category, location, description, name, email),
        )
        conn.commit()
        return jsonify(
            {"message": "Report submitted.", "reference_number": reference}
        ), 201
    finally:
        conn.close()