# eSHCAT — Hackathon Demo Script

> Full end-to-end run showing the citizen → staff → email → tracking flow.

## Setup (one-time)

```bash
python -m venv .venv
.venv\Scripts\activate
pip install -r backend/requirements.txt

flask --app backend.app init-db
flask --app backend.app seed-db

# optional email
copy backend\.env.example backend\.env   # then fill SMTP values

python -m backend.app
# laptop: http://127.0.0.1:5000
# phones on the same Wi-Fi: http://<laptop-ip>:5000
```

On the laptop, run `ipconfig` and use its Wi-Fi adapter's IPv4 address for `<laptop-ip>` (for example, `192.168.1.25`). If Windows Firewall asks, allow Python on private networks. The laptop must stay on and connected to Wi-Fi while serving the demo.

Reset anytime: delete `database/eshcat.sqlite`, re-run init-db + seed-db.

---

## Step 1 — Citizen opens eSHCAT

Homepage shows the hero: *"Electronic Services Hub for Catarman."* Click **Explore Services**.

## Step 2 — Browse

Filter or click **Death Certificate** (Civil Registry). The service page shows description, requirements, process, office info, and **Apply Online**.

## Step 3 — Apply

Fill the form (deceased info, purpose, preferred contact) plus applicant details. Click **Submit Application**.

## Step 4 — Reference number

Success screen shows a server-generated reference, e.g. **CAT-DC-7F3A91D2**. Note it. Click **Track Application** to see status: **Submitted**.

## Step 5 — Staff portal

Open `/pages/staff/login.html` (or Staff Login in the nav). Sign in:

- email `staff@eshcat.local`
- password `change_me_123`

The dashboard shows the new application in Recent Applications.

## Step 6 — Review

Click the reference → applicant and request details appear, plus a status form and history timeline.

Change status to **Under Review** (optional remarks) → **Update Status**.

## Step 7 — Email

If SMTP is configured, the applicant receives:

```
Subject: eSHCAT Application Update — CAT-DC-7F3A91D2
New Status: Under Review
```

Otherwise the email is queued in the `notifications` table (retry via `flask --app backend.app flush-queue`).

## Step 8 — Citizen tracking

Open **Track Request**, enter the reference. Status now reads **Under Review** with a timeline:

```
✓ Submitted → Received → ● Under Review
```

## Step 9 — Offline demo (optional)

1. DevTools → Network → Offline (or disconnect).
2. Submit an application → the app shows **Saved Locally · Pending Sync** (no fake reference).
3. Reconnect → draft auto-syncs and appears in the staff list.

## Step 10 — Forward (optional)

From application details, select a destination department → **Forward**. Status becomes **Forwarded**, visible in public tracking.

## Step 11 — MDRRMO emergency response (optional)

1. Tap the floating red **SOS** button on the public homepage. The Red-level SOS alert is sent immediately; allow location access if prompted. An optional dialog appears afterward for the accident type, landmark, and extra details. The MDRRMO map shows device GPS coordinates and the reported accuracy when the browser permits precise location access.
2. Sign in as `staff.drrmo@eshcat.local` with the configured seed password. The login opens the MDRRMO portal.
3. Tap **Share my location** and allow GPS access to set the responder route origin. The responder coordinates stay in that browser session and are not saved.
4. The incident map shows the responder and rescue locations with a suggested driving route when Google Maps can resolve both points. **Route & ETA in Google Maps** opens directions with the estimated travel time.
5. Click **Enable alarm sound** once so the browser can play the alert. New incidents appear in the live queue and sound a rising-and-falling ambulance-style siren.
6. Use the level selector to triage the incident to Yellow, Orange, or Red, then choose **Take response and silence alarm**. The responder's account is recorded. Mark the incident resolved when the response is complete.
7. Optionally send a different incident to **Ignore for me** and show that it remains visible to other MDRRMO staff.
8. Open **Response History** to review responded, resolved, and ignored incidents, then open **Profile** to show the signed-in staff account.

This is a hackathon demonstration flow, not a replacement for calling 911 or official MDRRMO dispatch channels. Exact locations and responder names are restricted to the MDRRMO portal. The public emergency feed is hidden from the landing page.

---

## Demo talking points

- **No citizen account** — frictionless access to services.
- **Reference-based tracking** — transparency without exposing personal data.
- **Department workflow + forwarding** — cross-office processing.
- **Offline resilience** — drafts sync later; no false confirmations.
- **Vanilla stack** — no frameworks; runs on plain HTML/CSS/JS + Flask.
- **Configurable services** — adding a new service only requires seed data.
