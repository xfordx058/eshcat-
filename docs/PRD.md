# eSHCAT — Product Requirements Document (PRD)

> **"One Municipality. Connected Services. Easier Access."**

**Team:** Walang Kanin Bossing  
**Project Type:** Digital Government / Civic Technology  
**Platform:** Responsive Web Application  
**Target Municipality:** Catarman, Northern Samar  
**Project Status:** Hackathon Prototype  
**Frontend:** HTML5 + CSS3 + Vanilla JavaScript (ES6+) — no React, Vue, Angular, Svelte, Next.js, Nuxt, jQuery, Bootstrap, Tailwind, or Material UI  
**Backend:** Flask / Python  
**Database:** SQLite  
**Email:** Gmail SMTP  

---

## 1. Product Overview

**eSHCAT (Electronic Services Hub for Catarman)** is a centralized web-based municipal services platform designed to make government services easier to access, track, and manage.

The platform connects citizens and municipal departments through one web application.

**Citizens can:**
- Browse municipal services
- View requirements
- Submit selected service applications
- Receive a reference number
- Track application status
- Request appointments
- Submit community concerns
- View announcements
- Find municipal office information

**Municipal staff can:**
- Log in securely
- View applications
- Review submitted information
- Update application status
- Request additional requirements
- Forward applications between departments
- Manage appointments
- Manage community reports
- Send email notifications
- View basic service statistics

---

## 2. Problem Statement

Our local government relies on disconnected and often manual systems, making public services slow and inefficient. Departments operate in silos, making it difficult to share data, coordinate tasks, and improve processes. This limits the LGU's ability to:
- Share information efficiently
- Coordinate departments
- Track requests
- Reduce repetitive manual work
- Provide transparent application status
- Respond quickly to community needs
- Modernize public services

---

## 3. Proposed Solution

One centralized web platform. Citizens no longer have to figure out which office to visit first — they discover services online and understand the required process.

**Citizen flow:** Open eSHCAT → Browse Services → Select Service → View Requirements → Complete Application → Submit → Receive Reference Number → Track Application → Receive Status Updates

**Staff flow:** Login → Dashboard → View Applications → Review Application → Update Status → Request / Verify Requirements → Forward / Approve / Reject → Notify Citizen

---

## 4. Product Goals

1. Centralize municipal service information.
2. Reduce unnecessary manual processes.
3. Provide application tracking.
4. Improve communication between citizens and departments.
5. Provide a shared workflow for municipal staff.
6. Support offline-friendly usage where practical.
7. Create a reusable service architecture.
8. Demonstrate how municipal services can be digitized.

---

## 5. User Types

### 5.1 Citizen
Citizens do **NOT** need an account. No username, password, registration, account, or authenticated dashboard. They can browse services, read requirements, submit supported applications, enter contact details, receive a reference number, track requests, request appointments, submit reports, and view announcements.

### 5.2 Municipal Staff
Staff must authenticate and access features based on their role and department. Possible roles: Staff, Department Head, Administrator, Auditor.

---

## 6. Platform Architecture

```text
Citizen Browser (HTML + CSS + Vanilla JS)
        │ HTTP / REST API
        ▼
Flask Backend (Python REST API / Services)
        │
   ┌────┴────┐
   ▼         ▼
 SQLite    Gmail SMTP
```

## 7. Technology Requirements

**Frontend (only):** HTML5, CSS3, Vanilla JavaScript, ES6+, Fetch API, LocalStorage, IndexedDB where necessary.

**Strictly no frameworks** — React, Vue, Angular, Svelte, Next.js, Nuxt, jQuery, Bootstrap, Tailwind, Material UI, or other frontend frameworks.

## 8. Frontend Architecture

See the implemented layout in `README.md`. Structure: `frontend/` with `index.html`, `pages/`, `css/`, `js/`, `js/staff/`, `assets/`, `manifest.json`, `service-worker.js`.

## 9–10. UI Design Direction & Public Website

Design direction: **Modern Minimalism + Soft Glassmorphism + Bento UI** — see `docs/UIUX.md`.

Main navigation: Home, Services, Track Request, Appointments, Report Concern, Announcements, Offices, About.

## 11. Home Page

- Hero: "Electronic Services Hub for Catarman — One Municipality. Connected Services. Easier Access."
- Search services
- Popular services
- How it works (4 steps)
- Announcements
- Emergency / important information

## 12–14. Service Directory

Browse services without logging in. Categories include Civil Registry, Business Permits, Treasurer, Assessor, Engineering, Planning, Health, Social Welfare, Agriculture, Environment, DRRM, Employment, Tourism, General Services. Each service card shows name, department, short description, estimated processing info, requirements, availability, and a View action. The details page shows full info plus `[Apply Online]` when available.

## 15–16. Online Service Application (Death Certificate)

The primary hackathon demonstration. **Prototype workflow only** — actual legal requirements, fees, identity verification, and release procedures must be validated with the appropriate municipal office before deployment.

Workflow: Citizen → Civil Registry → Death Certificate → Requirements → Application Form → Submit → Reference Number → Staff Review → Verification → Decision → Citizen Notification.

## 17–21. Application Form, Submission, Reference, Tracking, Timeline

- Collect only necessary information (name, email, mobile, address, request details).
- Validate on the frontend, re-validate on the backend.
- Reference numbers are **generated server-side** (e.g. `CAT-DC-7F3A91D2`, `CAT-APP-8A21F930`, `CAT-APT-93BC812A`, `CAT-REP-71A9D230`). The frontend never assumes a local reference means the server accepted the application.
- Tracking by reference shows service, department, status, last update, and timeline.
- Statuses: Submitted, Received, Under Review, Additional Requirements, For Verification, Forwarded, Approved, Rejected, Ready for Release, Completed, Cancelled.

## 22. Privacy Protection

Public tracking shows only reference, service, department, status, general timeline, and instructions. Never expose full personal records, sensitive documents, staff notes, or uploaded confidential files.

## 23–31. Staff Portal, Dashboard, Applications, History, Email

- Login at `/staff/login`; authentication and authorization happen on the backend.
- Dashboard shows counts (total, pending, under review, completed, appointments, reports).
- Application list columns: Reference, Service, Applicant, Department, Status, Submitted, Action.
- Application detail shows applicant info, form data, requirements, status, history, and actions (update status, request requirements, forward, approve, reject).
- Every status change creates a history record and can trigger a queued email notification.
- Gmail SMTP config via `.env` (never in JS/HTML/git). Emails are queued in a `notifications` table with retry.

## 32–35. Appointments, Community Reports, Announcements, Office Directory

- Appointments: office, date, time, name, email, mobile → `CAT-APT-...`
- Reports: category, location, description, contact optionally → `CAT-REP-...`
- Announcements: title, date, department, description, pinned flag
- Agency directory: office name, department, location, contact, email, hours, services

## 36. Configurable Service Engine

Services are data-driven: service → department → requirements → form fields → workflow → notifications. New services can be added via `database/seed.sql` without writing new code.

## 37–43. Backend Architecture, REST API, Database

See `docs/API.md`. Core tables: `departments`, `services`, `service_requirements`, `service_form_fields`, `staff_users`, `applications`, `application_history`, `notifications`, `appointments`, `community_reports`, `announcements`, `agency_directory`, `audit_logs`, `sync_queue`. Passwords are hashed (werkzeug). Staff actions are audit-logged.

## 44–47. Offline-Friendly Architecture & PWA

- Browser → Vanilla JS → Local Storage / IndexedDB → network? → Flask API / SQLite, else local queue → retry later.
- **Critical rule:** distinguish "Saved Locally / Pending Sync" from "Accepted by Server / reference number issued". Only the backend confirmation produces a real reference.
- Optional PWA: `manifest.json` + `service-worker.js`; cache public pages only, never staff pages or API responses.
- Show online/offline state; auto-sync queued submissions on reconnect.

## 48–51. Security, Frontend Security, Uploads, Data Privacy

- Backend auth, password hashing, role/department authorization, input validation, parameterized SQL, secure sessions, HTTPS in production, env-based secrets, audit logging, rate limiting where appropriate.
- Never trust client-side validation alone.
- Validate file type/size, rename, store outside public dirs, prevent executables, restrict access (when attachments are added).
- Prototype uses only synthetic/demo data.

## 52–56. Accessibility, Responsive, JS Standards, API Module, LocalStorage

- Semantic HTML, keyboard nav, labels, focus states, contrast, errors not color-only.
- Mobile-first with standard CSS media queries.
- Vanilla JS: `const`/`let`, modules, async/await, event delegation, centralized `js/api.js`.
- LocalStorage for UI prefs/drafts (non-sensitive only); IndexedDB for larger offline data.

## 57–67. Service Worker, Errors, Loading/Empty States, Staff Filtering, MVP Scope, Structure

Service worker must not cache sensitive pages. Clear network/validation/server/success messaging, loading states, duplicate-submit protection, empty states, staff filters by status/department/service/date/reference. Recommended structure implemented in this repository.

## 68–71. Demo & Build Plan

See `docs/DEMO.md` for the step-by-step script. Intended to fit an 8-hour and 24-hour build plan.

## 72–73. Key Differentiators & Roadmap

1. No citizen account
2. Centralized services
3. Reference-based tracking
4. Department workflow
5. Offline-friendly
6. Reusable architecture

Phase 1 (hackathon) → Phase 2 (pilot: more services, appointments, reports, routing) → Phase 3 (production: LGU integration, identity verification, security hardening).

## 74. Important Prototype Disclaimer

eSHCAT is a **hackathon prototype**. Service requirements, processing times, fees, legal procedures, document requirements, identity verification, data-retention policies, and actual government workflows must be validated with the appropriate LGU offices before any production deployment. No real confidential government records should be used during development or demonstration.

---

## 75. Team

# WALANG KANIN BOSSING

> **Walang kanin bossing. Pero may system. 😎**

## 76. Final Product Definition

**eSHCAT is a vanilla JavaScript-based digital municipal service platform that connects citizens and municipal departments through one accessible web application.**

Citizens: Discover → Apply → Receive Reference → Track → Get Updates  
Staff: Receive → Review → Process → Forward → Approve / Reject → Notify

> **One Municipality. Connected Services. Easier Access.**
>
> **Team: Walang Kanin Bossing**