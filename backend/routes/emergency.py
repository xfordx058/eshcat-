from flask import Blueprint, jsonify, request, session

from ..database import get_connection, log_audit
from ..routes.staff import login_required as staff_login_required

emergency_bp = Blueprint("emergency", __name__)


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