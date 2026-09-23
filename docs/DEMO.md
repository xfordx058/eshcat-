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
# open http://127.0.0.1:5000
```

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

---

## Demo talking points

- **No citizen account** — frictionless access to services.
- **Reference-based tracking** — transparency without exposing personal data.
- **Department workflow + forwarding** — cross-office processing.
- **Offline resilience** — drafts sync later; no false confirmations.
- **Vanilla stack** — no frameworks; runs on plain HTML/CSS/JS + Flask.
- **Configurable services** — adding a new service only requires seed data.