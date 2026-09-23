import json

from ..database import get_connection
from .notification_service import notify_application_update
from .reference_service import generate_reference

VALID_STATUSES = {
    "Submitted",
    "Received",
    "Under Review",
    "Additional Requirements",
    "For Verification",
    "Forwarded",
    "Approved",
    "Rejected",
    "Ready for Release",
    "Completed",
    "Cancelled",
}


def create_application(payload: dict) -> dict:
    """Create an application. Reference number is generated server-side."""
    service_id = payload.get("service_id")
    full_name = (payload.get("fullName") or "").strip()
    email = (payload.get("email") or "").strip()
    mobile = payload.get("mobile") or ""
    address = payload.get("address") or ""
    form_data = payload.get("form_data") or {}

    conn = get_connection()
    try:
        service = conn.execute(
            "SELECT * FROM services WHERE id = ?", (service_id,)
        ).fetchone()
        if service is None:
            raise ValueError("Service not found.")
        if service["is_online"] != 1:
            raise ValueError("Online application is currently unavailable for this service.")
        if not full_name or not email:
            raise ValueError("Full name and email are required.")

        reference = generate_reference("CAT")
        cursor = conn.execute(
            """
            INSERT INTO applications
                (reference_number, service_id, department_id, full_name, email, mobile, address, form_data, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'Submitted')
            """,
            (
                reference,
                service_id,
                service["department_id"],
                full_name,
                email,
                mobile,
                address,
                json.dumps(form_data, ensure_ascii=False),
            ),
        )
        app_id = cursor.lastrowid
        conn.execute(
            "INSERT INTO application_history (application_id, old_status, new_status, remarks) "
            "VALUES (?, NULL, 'Submitted', 'Application submitted.')",
            (app_id,),
        )
        conn.commit()
        return {"reference_number": reference, "application_id": app_id}
    finally:
        conn.close()


def get_application_by_reference(reference: str) -> dict:
    conn = get_connection()
    try:
        row = conn.execute(
            """
            SELECT a.*, s.name AS service_name, d.name AS department_name
            FROM applications a
            JOIN services s ON s.id = a.service_id
            JOIN departments d ON d.id = a.department_id
            WHERE a.reference_number = ?
            """,
            (reference.strip().upper(),),
        ).fetchone()
        if row is None:
            return None
        history = conn.execute(
            """
            SELECT old_status, new_status, remarks, created_at
            FROM application_history
            WHERE application_id = ?
            ORDER BY created_at DESC
            """,
            (row["id"],),
        ).fetchall()
        return {
            "reference_number": row["reference_number"],
            "service": row["service_name"],
            "department": row["department_name"],
            "status": row["status"],
            "updated_at": row["updated_at"],
            "created_at": row["created_at"],
            "timeline": [dict(h) for h in history],
        }
    finally:
        conn.close()


def update_status(application_id: int, staff_id: int, new_status: str, remarks: str = "") -> bool:
    if new_status not in VALID_STATUSES:
        raise ValueError("Invalid status.")

    conn = get_connection()
    try:
        row = conn.execute(
            "SELECT status FROM applications WHERE id = ?", (application_id,)
        ).fetchone()
        if row is None:
            return False
        old_status = row["status"]
        conn.execute(
            "UPDATE applications SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
            (new_status, application_id),
        )
        conn.execute(
            "INSERT INTO application_history (application_id, staff_id, old_status, new_status, remarks) "
            "VALUES (?, ?, ?, ?, ?)",
            (application_id, staff_id, old_status, new_status, remarks),
        )
        conn.commit()
        notify_application_update(application_id)
        return True
    finally:
        conn.close()
