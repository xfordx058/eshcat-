# eSHCAT

## Electronic Services Hub for Catarman

> **One Municipality. Connected Services. Easier Access.**

**Team:** Walang Kanin Bossing  
**Project Type:** Digital Government / Civic Technology  
**Target Municipality:** Catarman, Northern Samar  
**Platform:** Responsive Web Application  
**Frontend:** HTML5 + CSS3 + Vanilla JavaScript (ES6+) — no frameworks  
**Backend:** Flask / Python  
**Database:** SQLite  
**Email:** Gmail SMTP  
**Status:** Hackathon Prototype

---

## About

eSHCAT centralizes municipal services into one accessible web application. **Citizens do not need an account** — they can browse services, view requirements, submit applications, get a reference number, and track status. **Municipal staff** log in to review applications, update statuses, forward requests between departments, and notify applicants by email.

Inspired by the design direction *Modern Minimalism + Soft Glassmorphism + Bento UI*, the interface is built entirely with standard browser technologies — **no React, Vue, Angular, Bootstrap, or Tailwind**.

---

## Features

### Citizen (no account)

- Browse a configurable municipal service directory
- View requirements, process, and office information per service
- Submit applications online (Death Certificate primary demo)
- Receive a server-generated reference number
- Track application status with a full timeline
- Request office appointments
- Report community concerns
- View announcements and the office directory
- Send a one-tap MDRRMO SOS alert, then optionally add the incident type and details

### Staff

- Secure login (backend password hashing + sessions)
- Dashboard with service statistics
- Application list with status filters
- Application details with applicant info and form data
- Status updates with remarks and full history
- Forward applications between departments
- Email notifications to applicants (Gmail SMTP, queued retries)
- Audit logging for key actions

### Offline / PWA

- Offline detection banner
- Local drafts (LocalStorage) + IndexedDB submission queue
- **Important:** a reference number is only shown after the server confirms submission
- Service worker caching of public pages only (staff pages and API are never cached)
- Installable PWA (manifest + service worker)

---

## Tech Stack

| Layer      | Technology                                   |
| ---------- | -------------------------------------------- |
| Frontend   | HTML5, CSS3, Vanilla JS (ES6+), Fetch API    |
| Storage    | LocalStorage, IndexedDB                      |
| Backend    | Flask (Python 3)                             |
| Database   | SQLite (`database/schema.sql`, `seed.sql`)   |
| Email      | Gmail SMTP (`backend/services/email_service.py`) |

## Project Structure

```text
eshcat/
├── frontend/
│   ├── index.html
│   ├── manifest.json
│   ├── service-worker.js
│   ├── pages/            (services, service-details, apply, track,
│   │   │                  appointments, reports, announcements, offices)
│   │   └── staff/        (login, dashboard, applications, application-details)
│   ├── css/              (style, components, responsive, staff)
│   ├── js/               (vanilla JS modules + staff/)
│   └── assets/           (logo, icons)
├── backend/
│   ├── app.py            (Flask app + CLI commands)
│   ├── config.py, database.py
│   ├── routes/           (services, applications, tracking, appointments,
│   │                      reports, announcements, staff)
│   ├── services/         (application, reference, email, notification)
│   ├── models/
│   ├── tests/            (smoke_test.py, static_test.py)
│   ├── .env.example
│   └── requirements.txt
├── database/
│   ├── schema.sql
│   └── seed.sql
├── docs/
│   ├── PRD.md
│   ├── API.md
│   ├── DEMO.md
│   └── UIUX.md
└── README.md
```

---

## Getting Started

### 1. Backend

Requires Python 3.10+.

```bash
# Create a virtual environment (optional but recommended)
python -m venv .venv

# Activate (Windows)
.venv\Scripts\activate

# Install dependencies
pip install -r backend/requirements.txt

# Initialize and seed the database
flask --app backend.app init-db
flask --app backend.app seed-db
```

### 2. Email (optional)

Copy `backend/.env.example` to `backend/.env` and set:

```text
SMTP_USERNAME=your-dedicated@gmail.com
SMTP_PASSWORD=your-16-char-app-password
```

Never commit real SMTP credentials. The app still works without email enabled.

### 3. Run

```bash
python -m backend.app
# or: flask --app backend.app run --host=0.0.0.0 --port=5000
```

Start the server with `python -m backend.app` so it listens for connections from devices on your local network. On the host laptop, open `http://127.0.0.1:5000`. To connect a phone on the same Wi-Fi, find the laptop's IPv4 address with `ipconfig` and open `http://<laptop-ip>:5000` on the phone (for example, `http://192.168.1.25:5000`). Allow Python through Windows Firewall on private networks if prompted. Keep the laptop and phone on the same Wi-Fi network.

Mobile browsers generally require HTTPS to provide precise GPS to a page opened by local IP over Wi-Fi. If GPS is blocked during the HTTP demo, the SOS alert still reaches MDRRMO with an approximate Catarman location; add a nearby landmark in the optional dialog. Use a trusted HTTPS address to demonstrate device GPS.

### Demo accounts

| Role             | Email               | Password      |
| ---------------- | ------------------- | ------------- |
| Staff            | `staff@eshcat.local` | `change_me_123` |
| Department Head  | `head@eshcat.local`  | `change_me_123` |
| Administrator    | `admin@eshcat.local` | `change_me_123` |
| MDRRMO Responder | `staff.drrmo@eshcat.local` | `change_me_123` |
| MDRRMO Head      | `head.drrmo@eshcat.local`  | `change_me_123` |

Change `STAFF_SEED_PASSWORD` in `.env` before seeding non-demo environments.

---

## Testing

```bash
# API smoke test (services, applications, tracking, staff, notifications)
python backend/tests/smoke_test.py

# Static frontend serving test (all pages/assets return 200)
python backend/tests/static_test.py
```

Both tests use an isolated temp database.

---

## REST API (quick reference)

Full details in [`docs/API.md`](docs/API.md).

```http
Public
GET  /api/services
GET  /api/services/{id}
POST /api/applications
GET  /api/track/{reference}
POST /api/appointments
GET  /api/announcements
POST /api/reports

Staff (session-authenticated)
POST /api/staff/login
POST /api/staff/logout
GET  /api/staff/me
GET  /api/staff/dashboard
GET  /api/staff/applications
GET  /api/staff/applications/{id}
PATCH /api/staff/applications/{id}/status
POST /api/staff/applications/{id}/forward
```

---

## Death Certificate Demo Flow

1. Open the homepage → **Explore Services** → select **Death Certificate**.
2. Review requirements → **Apply Online**.
3. Complete the form → **Submit Application**.
4. Note the reference number (e.g. `CAT-DC-7F3A91D2`).
5. Staff → **Staff Login** → **Dashboard** shows the new application.
6. Open it → change status to **Under Review** → save.
7. Public **Track Request** now shows **Under Review**.

Full script in [`docs/DEMO.md`](docs/DEMO.md).

---

## Disclaimer

eSHCAT is a **hackathon prototype**. Requirements, processing times, fees, legal procedures, and workflows must be validated with the appropriate LGU offices before any production deployment. Only synthetic/demo data is used during development and judging.

---

## Team

# WALANG KANIN BOSSING

> **Walang kanin bossing. Pero may system.** 😎

Building technology for a more connected Catarman.
