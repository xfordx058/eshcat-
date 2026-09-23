"""Reusable branded email templates for resident notifications."""

from html import escape


def status_email(kind, reference, service, status, recipient_name="Resident", remarks=""):
    label = {"application": "application", "appointment": "appointment", "report": "community report"}.get(kind, "request")
    name = escape(recipient_name or "Resident")
    ref = escape(reference or "")
    service_name = escape(service or "eSHCAT services")
    current_status = escape(status or "Updated")
    note = escape(remarks or "No additional remarks were provided.").replace("\n", "<br>")
    subject = f"eSHCAT Update · {recipient_name or 'Resident'} · {reference} · {status}"
    text = (
        f"Hello {recipient_name or 'Resident'},\n\nYour {label} has been updated.\n\n"
        f"Requester: {recipient_name or 'Resident'}\nReference: {reference}\n"
        f"Service: {service or 'eSHCAT services'}\nStatus: {status}\n\n"
        f"Staff remarks: {remarks or 'No additional remarks were provided.'}\n\n"
        "Keep your reference number for future tracking.\n\neSHCAT · Electronic Services Hub of Catarman"
    )
    html = f"""<!doctype html>
<html><body style="margin:0;padding:0;background:#f4f7fb;font-family:Arial,Helvetica,sans-serif;color:#172033">
<table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background:#f4f7fb;padding:28px 12px"><tr><td align="center">
<table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="max-width:640px;background:#fff;border:1px solid #e5eaf1;border-radius:16px;overflow:hidden">
<tr><td style="padding:26px 32px;background:#0b2547"><img src="cid:eshcat-logo" alt="eSHCAT" width="150" style="display:block;width:150px;height:auto;margin:0 0 20px"><div style="font-size:11px;letter-spacing:1.6px;text-transform:uppercase;color:#a9c5e8">Electronic Services Hub of Catarman</div><div style="font-size:25px;line-height:32px;font-weight:700;color:#fff;margin-top:6px">Request status update</div></td></tr>
<tr><td style="padding:32px"><div style="font-size:16px;line-height:24px">Hello {name},</div><div style="font-size:15px;line-height:24px;color:#526174;margin-top:10px">Your {label} has been updated by the municipal service desk.</div>
<table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="margin-top:24px;border:1px solid #dce5f0;border-radius:12px;background:#f8fbff"><tr><td style="padding:20px 22px"><div style="font-size:11px;letter-spacing:1px;text-transform:uppercase;color:#728197">Requester</div><div style="font-size:15px;font-weight:700;margin-top:4px">{name}</div><div style="height:14px"></div><div style="font-size:11px;letter-spacing:1px;text-transform:uppercase;color:#728197">Reference number</div><div style="font-size:17px;font-weight:700;color:#0b5cab;margin-top:4px">{ref}</div><div style="height:14px"></div><div style="font-size:11px;letter-spacing:1px;text-transform:uppercase;color:#728197">Service</div><div style="font-size:14px;font-weight:600;margin-top:4px">{service_name}</div></td></tr><tr><td style="padding:0 22px 20px"><div style="font-size:11px;letter-spacing:1px;text-transform:uppercase;color:#728197">Current status</div><div style="display:inline-block;margin-top:8px;padding:8px 14px;border-radius:999px;background:#e6f0ff;color:#0b5cab;font-size:13px;font-weight:700">{current_status}</div></td></tr></table>
<div style="margin-top:20px;padding:16px 18px;border-left:4px solid #2563eb;background:#f5f9ff;border-radius:0 10px 10px 0"><div style="font-size:11px;letter-spacing:1px;text-transform:uppercase;color:#728197;font-weight:700">Staff remarks</div><div style="font-size:14px;line-height:22px;color:#29384d;margin-top:7px">{note}</div></div>
<div style="font-size:13px;line-height:20px;color:#728197;margin-top:24px">Keep your reference number for future tracking. If you have questions, please contact the responsible municipal office.</div></td></tr>
<tr><td style="padding:18px 32px;background:#f7f9fc;border-top:1px solid #e5eaf1;font-size:12px;line-height:18px;color:#728197">This is an automated message from eSHCAT · Electronic Services Hub of Catarman.</td></tr>
</table></td></tr></table></body></html>"""
    return subject, text, html
