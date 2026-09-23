from flask import Blueprint, jsonify

from ..database import get_connection

announcements_bp = Blueprint("announcements", __name__)


@announcements_bp.route("/api/announcements", methods=["GET"])
def list_announcements():
    conn = get_connection()
    try:
        rows = conn.execute(
            "SELECT * FROM announcements ORDER BY is_pinned DESC, created_at DESC"
        ).fetchall()
        return jsonify([dict(r) for r in rows])
    finally:
        conn.close()