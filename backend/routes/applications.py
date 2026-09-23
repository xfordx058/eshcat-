from flask import Blueprint, jsonify, request

from ..services.application_service import create_application

applications_bp = Blueprint("applications", __name__)


@applications_bp.route("/api/applications", methods=["POST"])
def submit_application():
    payload = request.get_json(silent=True) or {}
    try:
        result = create_application(payload)
    except ValueError as exc:
        return jsonify({"error": str(exc)}), 400
    return jsonify(
        {
            "message": "Application submitted successfully.",
            "reference_number": result["reference_number"],
        }
    ), 201