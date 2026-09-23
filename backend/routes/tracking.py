from flask import Blueprint, jsonify, request

from ..services.application_service import get_application_by_reference

tracking_bp = Blueprint("tracking", __name__)


@tracking_bp.route("/api/track/<reference>", methods=["GET"])
def track(reference):
    result = get_application_by_reference(reference)
    if result is None:
        return jsonify({"error": "No application found for that reference number."}), 404
    return jsonify(result)