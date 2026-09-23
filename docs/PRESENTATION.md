# eSHCAT
## Electronic Services Hub for Catarman

**One Municipality. Connected Services. Easier Mobile Access.**

### Team Walang Kanin Bossing
Digital Government / Civic Technology  
Catarman, Northern Samar  
Hackathon Prototype

<!-- Speaker notes: Introduce eSHCAT as a simple digital front door for municipal services. -->

---

# The Problem

Residents often need to:

- Visit different offices to ask for requirements
- Follow up manually on applications and appointments
- Repeat the same information to different staff members
- Report community concerns without a clear tracking process

**The result:** longer transactions, limited visibility, and less convenient public service.

<!-- Speaker notes: Focus on the resident experience. The problem is not only paperwork; it is uncertainty and repeated effort. -->

---

# Our Solution

## Municipal services, made simple on mobile

eSHCAT brings Catarman's services into one mobile-friendly app experience. Residents can install it from a compatible browser and open it like an app, with the same core services available through the website. The focus is simple: help residents find what they need, send a request, and check its progress from their phone.

Residents can:

- Explore services and requirements
- Submit applications without creating an account
- Book office appointments
- Report community concerns with optional photo evidence
- Track updates using a reference number

The app keeps the same services and request process as the web version, with layouts and quick actions designed for convenient use on a phone. No citizen account is required.

<!-- Speaker notes: Emphasize that no account is required for the public-facing services. -->

---

# Mobile Resident Journey

```text
Discover a service
        ↓
Review requirements and processing information
        ↓
Submit an application, appointment, or concern
        ↓
Receive a reference number
        ↓
Track status and timeline online
```

**A simpler way to access the same municipal services from a phone.**

<!-- Speaker notes: This is the main story of the product. Walk through it as a single connected experience. -->

---

# Core Citizen Features

The mobile app brings the public-facing eSHCAT functions together in one place. Residents can use the same service directory, applications, appointments, reports, announcements, and request tracking available on the web.

### Municipal service directory
Configurable offices, services, requirements, fees, and estimated processing times.

### Online applications
Digital forms collect the information needed by the selected office.

### Appointments
Residents select an office, service, date, and time for a planned visit.

### Community reports
Residents can submit concerns such as road damage, waste management, or other local issues.

### Announcements and office directory
Important information is available in one public portal.

---

# COMELEC Services

eSHCAT now includes a dedicated Commission on Elections workspace for Catarman.

Residents can request:

- Voter’s Certification
- Transfer of Voter Registration

The forms collect the relevant voter information, precinct details, address information, valid government-issued ID, and transaction purpose.

COMELEC requirements are shown before submission so residents know what to prepare.

---

# Election-day Queue

When enabled by COMELEC:

1. A resident requests a present number
2. eSHCAT assigns the next number and COMELEC room
3. The resident searches the present number online
4. The page shows the current queue status and assigned room

COMELEC staff can turn queue intake and present-number search on or off independently.

<!-- Speaker notes: Demonstrate the public COMELEC page with a sample present number. Explain that search can remain available for already-issued numbers even when new intake is closed. -->

---

# Tracking That Builds Trust

Every submission receives a server-generated reference number.

The tracking page shows:

- Current status
- Status history and timeline
- Staff remarks when available
- Appointment schedule for appointment requests
- Updates for applications, appointments, and community reports

**Residents know what happened, what is happening, and what comes next.**

<!-- Speaker notes: Demonstrate tracking with a sample reference number. Point out that appointment requests use the same tracking experience. -->

---

# Staff Operations Portal

Staff can securely log in to:

- View applications, appointments, and community reports
- Search and filter records by status
- Open complete record details
- Review attached report photos
- Update status and add remarks
- Forward requests to the appropriate department
- View dashboards and operational statistics

Role-based access supports general staff, department heads, and administrators.

---

# COMELEC Staff Workflow

The COMELEC dashboard is department-scoped. Only COMELEC staff, COMELEC department heads, and administrators can view COMELEC transactions.

The dedicated application view includes:

- Applicant and transaction information
- Voter certification or transfer details
- Approve, reject, and update-status actions
- Request Requirements workflow
- Staff remarks and email notifications
- Complete application history and timeline

Administrators can also search the COMELEC staff list and add staff accounts assigned to COMELEC.

---

# Status Updates and Notifications

When staff update a record:

1. The record status is validated
2. Duplicate status updates are prevented
3. A history entry is saved
4. Staff remarks are retained
5. The requester can receive an email notification

The email includes the requester’s name, reference number, new status, remarks, and eSHCAT branding.

<!-- Speaker notes: Highlight the audit trail and idempotency. Saving the same status twice should not create a false success or duplicate update. -->

---

# Reports and Accountability

The staff Reports page provides:

- A list of community concerns
- Attached photo viewing when submitted
- Status filtering: Open, In Progress, and Resolved
- Record detail view and status updates
- Daily report totals with a visual ring summary
- Downloadable PDF report for today’s submissions

This gives offices a clearer view of incoming concerns and completed work.

---

# Technology Behind eSHCAT

| Layer | Technology |
| --- | --- |
| Frontend | HTML5, CSS3, Vanilla JavaScript |
| Backend | Flask / Python |
| Database | SQLite |
| Communication | Gmail SMTP email notifications |
| Design | Mobile-friendly responsive interface with accessible components |
| App experience | Installable Progressive Web App (PWA) |

The system uses standard web technologies with no frontend framework dependency. The mobile app connects to eSHCAT's online services and can be added to a compatible device's home screen for quick access. A reference number is shown after the server confirms a submission.

---

# Demo Flow

## Recommended live presentation sequence

1. Open eSHCAT on a phone and show the mobile home screen and quick actions
2. If available, show how eSHCAT can be installed from the browser
3. Browse a municipal service and its requirements
4. Submit a sample application or appointment
5. Copy the generated reference number and open **Track Request**
6. Show the request timeline, then log in to the Staff Portal
7. Open the record, update its status, and add a remark
8. Return to mobile tracking and show the updated status
9. Open Reports and show the daily summary/PDF option
10. Open the COMELEC page and demonstrate present-number search and room assignment
11. Open the COMELEC dashboard and review a voter transaction history

<!-- Speaker notes: Use synthetic data only. Do not present real citizen information. -->

---

# Value to the Municipality

### For residents
The same municipal services in a simple, mobile-friendly app experience, with clearer requirements, fewer manual follow-ups, and better visibility.

### For staff
Centralized records, faster review, structured status workflows, and searchable history.

### For Catarman
A practical foundation for more accessible, transparent, and connected local government services.

---

# Roadmap

Potential next steps for production readiness:

- Validate workflows and requirements with each municipal office
- Add stronger identity verification and document security
- Connect to official government records where appropriate
- Add analytics for response time and service performance
- Continue improving accessibility, localization, and the mobile experience
- Deploy with production monitoring, backups, and security review

**eSHCAT is a strong starting point—not a replacement for official validation and governance.**

---

# Closing

## eSHCAT makes municipal services easier to access on mobile, discover, request, track, and manage.

**One Municipality. Connected Services. Easier Mobile Access.**

### Team Walang Kanin Bossing

Thank you.

<!-- Speaker notes: End with the product promise and invite questions or a live demo. -->
