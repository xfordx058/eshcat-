from ..database import get_connection
from .email_templates import status_email
from .email_service import queue_email, send_email


def notify_application_update(application_id: int, force_send: bool = True) -> bool:
    """Send an email notification to the applicant for a status update."""
    conn = get_connection()
    try:
        app = conn.execute(
            """
            SELECT a.reference_number, a.email, a.full_name, a.status,
                   (SELECT h.remarks FROM application_history h
                    WHERE h.application_id = a.id ORDER BY h.id DESC LIMIT 1) AS remarks,
                   s.name AS service_name
            FROM applications a
            JOIN services s ON s.id = a.service_id
            WHERE a.id = ?
            """,
            (application_id,),
        ).fetchone()
        if app is None or not app["email"]:
            return False

        requester_name = app["full_name"] or "Resident"
        remarks = app["remarks"] or ""
        subject = f"eSHCAT Update — {requester_name} — {app['reference_number']}"
        message = (
            f"Hello {requester_name},\n\n"
            "Your eSHCAT application has been updated.\n\n"
            f"Requester:\n{requester_name}\n\n"
            f"Reference:\n{app['reference_number']}\n\n"
            f"Service:\n{app['service_name']}\n\n"
            f"New Status:\n{app['status']}\n\n"
            f"Staff remarks:\n{remarks or 'No additional remarks were provided.'}\n\n"
            "Please use your reference number to track your request."
        )

        _, _, html = status_email("application", app["reference_number"], app["service_name"], app["status"], requester_name, remarks)
        if force_send and send_email(app["email"], subject, message, html):
            conn.execute(
                "INSERT INTO notifications (application_id, recipient_email, subject, message, status, sent_at) "
                "VALUES (?, ?, ?, ?, 'Sent', datetime('now'))",
                (application_id, app["email"], subject, message),
            )
            conn.commit()
            return True

        queue_email(conn, application_id, app["email"], subject, message)
        conn.commit()
        return True
    finally:
        conn.close()


def notify_civil_update(record_type: str, record_id: int, force_send: bool = True) -> bool:
    """Notify the resident after an appointment or report status update."""
    if record_type == "appointment":
        query = "SELECT reference_number, email, full_name, status, remarks, appointment_date AS service FROM appointments WHERE id = ?"
    else:
        query = "SELECT reference_number, email, name AS full_name, status, remarks, category AS service FROM community_reports WHERE id = ?"
    conn = get_connection()
    try:
        row = conn.execute(query, (record_id,)).fetchone()
        if row is None or not row["email"]:
            return False
        subject, message, html = status_email(record_type, row["reference_number"], row["service"], row["status"], row["full_name"] or "Resident", row["remarks"] or "")
        if force_send and send_email(row["email"], subject, message, html):
            conn.execute("INSERT INTO notifications (recipient_email, subject, message, status, sent_at) VALUES (?, ?, ?, 'Sent', CURRENT_TIMESTAMP)", (row["email"], subject, message))
            conn.commit()
            return True
        queue_email(conn, None, row["email"], subject, message)
        conn.commit()
        return True
    finally:
        conn.close()
