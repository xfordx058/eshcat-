import json
import re
import sqlite3

from flask import Blueprint, Response, jsonify, request
from werkzeug.exceptions import BadRequest

from ..database import get_connection

public_bp = Blueprint("services", __name__)


def _build_form_pdf(title: str, department: str, fields: list, requirements: list) -> bytes:
    """Build a small dependency-free blank application form PDF."""
    lines = [
        "MUNICIPALITY OF CATARMAN",
        department.upper(),
        title.upper(),
        "APPLICATION FORM",
        "",
        "Applicant / requester information",
        "Please complete the fields below and submit this form to the office.",
        "",
    ]
    for field in fields:
        marker = "*" if field["required"] else "(optional)"
        lines.append(f"{field['label']} {marker}: ________________________________________________")
    lines.extend([
        "",
        "Required documents",
    ])
    if requirements:
        lines.extend([f"[ ] {item}" for item in requirements])
    else:
        lines.append("[ ] Documents specified by the responsible office")
    lines.extend([
        "",
        "Applicant signature: ______________________________    Date: ______________",
        "",
        "For office use only",
        "Received by: ______________________________________    Date: ______________",
        "Remarks: ________________________________________________________________",
    ])

    # Keep the generated PDF compatible with standard viewers without adding a
    # third-party PDF dependency. Non-Latin characters are replaced safely.
    safe_lines = [line.encode("latin-1", "replace").decode("latin-1") for line in lines]
    pages = [safe_lines[i:i + 38] for i in range(0, len(safe_lines), 38)] or [[]]
    objects = [None, None, b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"]
    page_ids = []
    for page_lines in pages:
        content = ["BT", "/F1 10 Tf", "50 750 Td", "14 TL"]
        for index, line in enumerate(page_lines):
            escaped = line.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")
            content.append(f"({escaped}) Tj")
            if index < len(page_lines) - 1:
                content.append("T*")
        content.append("ET")
        content_bytes = "\n".join(content).encode("latin-1")
        content_id = len(objects) + 1
        objects.append(f"<< /Length {len(content_bytes)} >>\nstream\n".encode("latin-1") + content_bytes + b"\nendstream")
        page_id = len(objects) + 1
        objects.append(None)
        page_ids.append((page_id, content_id))

    objects[0] = b"<< /Type /Catalog /Pages 2 0 R >>"
    kids = " ".join(f"{page_id} 0 R" for page_id, _ in page_ids)
    objects[1] = f"<< /Type /Pages /Kids [{kids}] /Count {len(page_ids)} >>".encode("latin-1")
    for page_id, content_id in page_ids:
        objects[page_id - 1] = (
            f"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] "
            f"/Resources << /Font << /F1 3 0 R >> >> /Contents {content_id} 0 R >>"
        ).encode("latin-1")

    pdf = bytearray(b"%PDF-1.4\n%\xe2\xe3\xcf\xd3\n")
    offsets = [0]
    for object_id, obj in enumerate(objects, 1):
        offsets.append(len(pdf))
        pdf.extend(f"{object_id} 0 obj\n".encode("latin-1"))
        pdf.extend(obj)
        pdf.extend(b"\nendobj\n")
    xref = len(pdf)
    pdf.extend(f"xref\n0 {len(objects) + 1}\n0000000000 65535 f \n".encode("latin-1"))
    for offset in offsets[1:]:
        pdf.extend(f"{offset:010d} 00000 n \n".encode("latin-1"))
    pdf.extend(
        f"trailer\n<< /Size {len(objects) + 1} /Root 1 0 R >>\nstartxref\n{xref}\n%%EOF\n".encode("latin-1")
    )
    return bytes(pdf)


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


@public_bp.route("/api/offices", methods=["GET"])
def list_offices():
    """Public office directory with real contact info + services."""
    conn = get_connection()
    try:
        rows = conn.execute(
            """
            SELECT id, name, description, location, contact_number, email, office_hours
            FROM departments
            ORDER BY name
            """
        ).fetchall()
        offices = []
        for row in rows:
            department = dict(row)
            services = conn.execute(
                """
                SELECT id, name, is_online
                FROM services
                WHERE department_id = ?
                ORDER BY name
                """,
                (row["id"],),
            ).fetchall()
            department["services"] = [
                {"id": s["id"], "name": s["name"], "is_online": bool(s["is_online"])}
                for s in services
            ]
            offices.append(department)
        return jsonify(offices)
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


@public_bp.route("/api/services/<int:service_id>/form.pdf", methods=["GET"])
def download_service_form(service_id):
    conn = get_connection()
    try:
        row = conn.execute(
            """
            SELECT s.name, d.name AS department_name
            FROM services s
            JOIN departments d ON d.id = s.department_id
            WHERE s.id = ?
            """,
            (service_id,),
        ).fetchone()
        if row is None:
            return jsonify({"error": "Service not found."}), 404

        fields = conn.execute(
            "SELECT label, required FROM service_form_fields WHERE service_id = ? ORDER BY id",
            (service_id,),
        ).fetchall()
        requirements = conn.execute(
            "SELECT requirement FROM service_requirements WHERE service_id = ? ORDER BY id",
            (service_id,),
        ).fetchall()
        pdf = _build_form_pdf(
            row["name"],
            row["department_name"],
            [{"label": field["label"], "required": bool(field["required"])} for field in fields],
            [item["requirement"] for item in requirements],
        )
        slug = re.sub(r"[^a-z0-9]+", "-", row["name"].lower()).strip("-")
        filename = f"{slug or 'service'}-application-form.pdf"
        return Response(
            pdf,
            mimetype="application/pdf",
            headers={"Content-Disposition": f'attachment; filename="{filename}"'},
        )
    finally:
        conn.close()
