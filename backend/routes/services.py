import json
import sqlite3

from flask import Blueprint, jsonify, request
from werkzeug.exceptions import BadRequest

from ..database import get_connection

public_bp = Blueprint("services", __name__)


def _serialize_service(conn, row) -> dict:
    return {
        "id": row["id"],
        "department_id": row["department_id"],
        "department": row["department_name"],
        "name": row["name"],
        "short_description": row["short_description"],
        "description": row["description"],
        "estimated_processing": row["estimated_processing"],
        "is_online": bool(row["is_online"]),
        "requirements": [],  # attached by caller when needed
    }


@public_bp.route("/api/services", methods=["GET"])
def list_services():
    conn = get_connection()
    try:
        rows = conn.execute(
            """
            SELECT s.*, d.name AS department_name
            FROM services s
            JOIN departments d ON d.id = s.department_id
            ORDER BY s.name
            """
        ).fetchall()
        services = []
        for row in rows:
            svc = _serialize_service(conn, row)
            requirements = conn.execute(
                "SELECT requirement FROM service_requirements WHERE service_id = ?",
                (row["id"],),
            ).fetchall()
            svc["requirements"] = [r["requirement"] for r in requirements]
            services.append(svc)
        return jsonify(services)
    finally:
        conn.close()


@public_bp.route("/api/services/<int:service_id>", methods=["GET"])
def get_service(service_id):
    conn = get_connection()
    try:
        row = conn.execute(
            """
            SELECT s.*, d.name AS department_name
            FROM services s
            JOIN departments d ON d.id = s.department_id
            WHERE s.id = ?
            """,
            (service_id,),
        ).fetchone()
        if row is None:
            return jsonify({"error": "Service not found."}), 404
        svc = _serialize_service(conn, row)
        svc["requirements"] = [
            r["requirement"]
            for r in conn.execute(
                "SELECT requirement FROM service_requirements WHERE service_id = ?",
                (service_id,),
            ).fetchall()
        ]
        fields = conn.execute(
            "SELECT label, field_name, field_type, required, options FROM service_form_fields WHERE service_id = ? ORDER BY id",
            (service_id,),
        ).fetchall()
        form_fields = []
        for f in fields:
            options = None
            if f["options"]:
                try:
                    options = json.loads(f["options"])
                except (ValueError, TypeError):
                    options = None
            form_fields.append(
                {
                    "label": f["label"],
                    "field_name": f["field_name"],
                    "field_type": f["field_type"],
                    "required": bool(f["required"]),
                    "options": options,
                }
            )
        svc["form_fields"] = form_fields
        office = conn.execute(
            "SELECT * FROM departments WHERE id = ?", (row["department_id"],)
        ).fetchone()
        svc["office"] = dict(office) if office else None
        return jsonify(svc)
    except sqlite3.Error as exc:
        raise BadRequest(str(exc))
    finally:
        conn.close()