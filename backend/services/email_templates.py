"""Reusable branded email templates for resident notifications."""

from html import escape


def status_email(kind, reference, service, status, recipient_name="Resident", remarks=""):
    label = {"application": "application", "appointment": "appointment", "report": "community report"}.get(kind, "request")
    name = escape(recipient_name or "Resident")
    ref = escape(reference or "")
    service_name = escape(service or "eSHCAT services")
    current_status = escape(status or "Updated")
    note = escape(remarks or "No additional remarks were provided.")
    subject = f"eSHCAT Update · {recipient_name or 'Resident'} · {reference} · {status}"
    text = (f"Hello {recipient_name or 'Resident'},\n\nYour {label} has been updated.\n\n"
            f"Reference: {reference}\nService: {service or 'eSHCAT services'}\nStatus: {status}\n\n"
            f"Staff remarks: {remarks or 'No additional remarks were provided.'}\n\n"
            "Keep your reference number for tracking.\n\neSHCAT · Electronic Services Hub of Catarman")
    html = f"""<!doctype html><html><body style="margin:0;background:#f1f5f9;font-family:Arial,sans-serif;color:#172033">
<div style="max-width:620px;margin:32px auto;background:#fff;border-radius:18px;overflow:hidden;box-shadow:0 8px 30px rgba(15,23,42,.10)">
<div style="padding:24px 32px;background:#0b2547;color:#fff"><img src="cid:eshcat-logo" alt="eSHCAT" style="display:block;width:150px;max-width:100%;height:auto;margin-bottom:18px"><h1 style="margin:0;font-size:24px">Request status update</h1></div>
<div style="padding:30px 32px"><p style="font-size:16px">Hello {name},</p><p>Your {label} has been updated by the municipal service desk.</p>
<div style="margin:24px 0;padding:20px;border:1px solid #dbe5f1;border-radius:14px;background:#f8fbff"><div style="font-size:12px;color:#64748b;text-transform:uppercase;letter-spacing:1px">Reference</div><div style="font-size:18px;font-weight:700;color:#0b2547;margin:5px 0 16px">{ref}</div><div style="font-size:13px;color:#64748b">Service</div><div style="font-weight:600;margin:4px 0 14px">{service_name}</div><div style="font-size:13px;color:#64748b">Current status</div><span style="display:inline-block;margin-top:6px;padding:8px 12px;border-radius:999px;background:#dbeafe;color:#2563eb;font-weight:700">{current_status}</span></div>
<p style="font-size:13px;color:#64748b;margin-bottom:5px">Staff remarks</p><p style="margin-top:0">{note}</p><p style="margin-top:28px;font-size:13px;color:#64748b">Keep your reference number for future tracking.</p></div><div style="padding:18px 32px;background:#f8fafc;color:#64748b;font-size:12px">eSHCAT · Electronic Services Hub of Catarman</div></div></body></html>"""
    return subject, text, html
