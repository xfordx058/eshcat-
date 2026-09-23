import os
import smtplib
import ssl

from email.mime.image import MIMEImage
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

from .. import config
from ..database import get_connection


def send_email(recipient: str, subject: str, message: str, html_message: str = "") -> bool:
    """Send an email via Gmail SMTP. Returns True on success."""
    if not config.EMAIL_ENABLED:
        return False

    msg = MIMEMultipart("related")
    msg["Subject"] = subject
    msg["From"] = config.MAIL_FROM
    msg["To"] = recipient
    alternative = MIMEMultipart("alternative")
    alternative.attach(MIMEText(message, "plain", "utf-8"))
    if html_message:
        alternative.attach(MIMEText(html_message, "html", "utf-8"))
    msg.attach(alternative)

    logo_path = os.path.join(
        os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))),
        "frontend", "assets", "images", "eSHCATlofogo.png",
    )
    if html_message and os.path.exists(logo_path):
        with open(logo_path, "rb") as logo_file:
            logo = MIMEImage(logo_file.read(), _subtype="png")
        logo.add_header("Content-ID", "<eshcat-logo>")
        logo.add_header("Content-Disposition", "inline", filename="eSHCATlofogo.png")
        msg.attach(logo)

    context = ssl.create_default_context()
    try:
        with smtplib.SMTP_SSL(config.SMTP_HOST, config.SMTP_PORT, context=context) as server:
            server.login(config.SMTP_USERNAME, config.SMTP_PASSWORD)
            server.sendmail(config.SMTP_USERNAME, [recipient], msg.as_string())
        return True
    except Exception as exc:  # pragma: no cover - depends on external service
        process_failed_email(exc, subject, message)
        return False


def queue_email(conn, application_id, recipient, subject, message) -> None:
    conn.execute(
        "INSERT INTO notifications (application_id, recipient_email, subject, message) VALUES (?, ?, ?, ?)",
        (application_id, recipient, subject, message),
    )


def process_failed_email(error, subject, message) -> None:
    """Store a failed email for retry."""
    conn = get_connection()
    try:
        conn.execute(
            "INSERT INTO notifications (recipient_email, subject, message, status, last_error) VALUES (?, ?, ?, 'Failed', ?)",
            ("", subject, message, str(error)[:500]),
        )
        conn.commit()
    finally:
        conn.close()


def flush_email_queue(max_attempts: int = 3) -> dict:
    """Attempt to send queued emails. Returns report."""
    conn = get_connection()
    report = {"sent": 0, "failed": 0}
    try:
        rows = conn.execute(
            "SELECT * FROM notifications WHERE status IN ('Pending', 'Failed') AND retry_count < ?",
            (max_attempts,),
        ).fetchall()
        for row in rows:
            ok = send_email(row["recipient_email"], row["subject"], row["message"])
            if ok:
                conn.execute(
                    "UPDATE notifications SET status = 'Sent', sent_at = datetime('now') WHERE id = ?",
                    (row["id"],),
                )
                report["sent"] += 1
            else:
                conn.execute(
                    "UPDATE notifications SET retry_count = retry_count + 1, last_error = 'SMTP unavailable', status = 'Failed' WHERE id = ?",
                    (row["id"],),
                )
                report["failed"] += 1
        conn.commit()
    finally:
        conn.close()
    return report
