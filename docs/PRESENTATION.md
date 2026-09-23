# eSHCAT
## Electronic Services Hub for Catarman

**One Municipality. Connected Services. Easier Access.**

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

## One digital hub for Catarman services

eSHCAT connects residents and municipal staff through one responsive web application.

Residents can:

- Explore services and requirements
- Submit applications without creating an account
- Book office appointments
- Report community concerns with optional photo evidence
- Track updates using a reference number

<!-- Speaker notes: Emphasize that no account is required for the public-facing services. -->

---

# Resident Journey

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

**Clear next steps. Fewer follow-ups. Better visibility.**

<!-- Speaker notes: This is the main story of the product. Walk through it as a single connected experience. -->

---

# Core Citizen Features

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
| Offline support | Local drafts, IndexedDB queue, service worker |
| Design | Responsive Bento UI with accessible components |

The system uses standard web technologies with no frontend framework dependency.

---

# Demo Flow

## Recommended live presentation sequence

1. Open the public homepage
2. Browse a municipal service and its requirements
3. Submit a sample application or appointment
4. Copy the generated reference number
5. Open **Track Request** and show the initial timeline
6. Log in to the Staff Portal
7. Open the record, update its status, and add a remark
8. Return to tracking and show the updated status
9. Open Reports and show the daily summary/PDF option

<!-- Speaker notes: Use synthetic data only. Do not present real citizen information. -->

---

# Value to the Municipality

### For residents
Convenient access, clearer requirements, fewer manual follow-ups, and better visibility.

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
- Expand accessibility, localization, and mobile testing
- Deploy with production monitoring, backups, and security review

**eSHCAT is a strong starting point—not a replacement for official validation and governance.**

---

# Closing

## eSHCAT makes municipal services easier to discover, request, track, and manage.

**One Municipality. Connected Services. Easier Access.**

### Team Walang Kanin Bossing

Thank you.

<!-- Speaker notes: End with the product promise and invite questions or a live demo. -->
