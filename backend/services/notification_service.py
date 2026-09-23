from ..database import get_connection
from .email_service import queue_email, send_email


def notify_application_update(application_id: int, force_send: bool = True) -> bool:
    """Send an email notification to the applicant for a status update."""
    conn = get_connection()
    try:
        app = conn.execute(
            """
            SELECT a.reference_number, a.email, a.status, s.name AS service_name
            FROM applications a
            JOIN services s ON s.id = a.service_id
            WHERE a.id = ?
            """,
            (application_id,),
        ).fetchone()
        if app is None or not app["email"]:
            return False

        subject = f"eSHCAT Application Update — {app['reference_number']}"
        message = (
            "Your eSHCAT application has been updated.\n\n"
            f"Reference:\n{app['reference_number']}\n\n"
            f"Service:\n{app['service_name']}\n\n"
            f"New Status:\n{app['status']}\n\n"
            "Please use your reference number to track your request."
        )

        if force_send and send_email(app["email"], subject, message):
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