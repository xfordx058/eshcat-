# eSHCAT — REST API Reference

Base URL: `http://127.0.0.1:5000/api`

All public endpoints return JSON. Staff endpoints require a logged-in session (cookie, `credentials: include`). Errors return `{"error": "message"}` with the appropriate status code.

## Public

### `GET /services`
List all services with department, description, estimated processing, availability, and requirements.

**Response:** `200`
```json
[
  {
    "id": 1,
    "department_id": 1,
    "department": "Civil Registry",
    "name": "E-Death Certificate",
    "short_description": "...",
    "description": "...",
    "estimated_processing": "5-7 working days",
    "is_online": true,
    "requirements": ["..."]
  }
]
```

### `GET /services/{id}`
Single service including `form_fields` (configurable form definition) and `office` information.

**Response:** `200`
```json
{
  "id": 1,
  "name": "E-Death Certificate",
  "form_fields": [
    { "label": "Full Name", "field_name": "fullName", "field_type": "text", "required": true, "options": null }
  ],
  "office": { "location": "Municipal Hall, Catarman", "contact_number": "(055) 555-0101" }
}
```
**Errors:** `404` service not found.

### `POST /applications`
Submit a service application. Returns a **server-generated** reference number.

**Request body:**
```json
{
  "service_id": 1,
  "fullName": "Juan Dela Cruz",
  "email": "juan@example.com",
  "mobile": "09171234567",
  "address": "Catarman, Northern Samar",
  "form_data": { "deceasedName": "...", "purpose": "Legal affairs" }
}
```
**Response:** `201`
```json
{ "message": "Application submitted successfully.", "reference_number": "CAT-DC-7F3A91D2" }
```
**Errors:** `400` missing fields / service not online.

### `GET /track/{reference}`
Public tracking by reference number. Returns service, department, status, timestamps, and timeline.

**Response:** `200`
```json
{
  "reference_number": "CAT-DC-7F3A91D2",
  "service": "E-Death Certificate",
  "department": "Civil Registry",
  "status": "Under Review",
  "created_at": "...",
  "updated_at": "...",
  "timeline": [
    { "old_status": null, "new_status": "Submitted", "remarks": "...", "created_at": "..." }
  ]
}
```
**Errors:** `404` no application found.

### `POST /appointments`
```json
{
  "department_id": 1, "service_id": null,
  "date": "2026-10-01", "time": "10:00 AM",
  "full_name": "...", "email": "...", "mobile": "..."
}
```
**Response:** `201` `{ "reference_number": "CAT-APT-..." }`

### `POST /reports`
```json
{ "category": "Road problem", "location": "...", "description": "...", "name": "...", "email": "..." }
```
**Response:** `201` `{ "reference_number": "CAT-REP-..." }`

### `GET /announcements`
List announcements, pinned first.

## Staff (authenticated)

### `POST /staff/login`
```json
{ "email": "staff@eshcat.local", "password": "change_me_123" }
```
**Response:** `200` staff profile. **Errors:** `401` invalid credentials.

### `POST /staff/logout`
Clears the session. **Response:** `200`.

### `GET /staff/me`
Returns the logged-in staff profile. **Errors:** `401` not logged in.

### `GET /staff/dashboard`
```json
{
  "total": 42, "today": 3, "Pending": 8, "Received": 2, "Under Review": 5,
  "Approved": 4, "Rejected": 1, "Completed": 21, "appointments": 2, "reports": 1
}
```

### `GET /staff/applications`
Optional query param `?status=Under Review`. Returns list of applications (id, reference, service, applicant, department, status, created_at).

### `GET /staff/applications/{id}`
Full application: applicant info, `form_data`, history, department. Also writes a `VIEW_APPLICATION` audit log.

### `PATCH /staff/applications/{id}/status`
```json
{ "status": "Under Review", "remarks": "Validating details." }
```
Creates a history record and queues/emails a notification. **Errors:** `400` invalid status, `404` not found.

### `POST /staff/applications/{id}/forward`
```json
{ "department_id": 4, "remarks": "Route to Engineering" }
```
Sets status to `Forwarded`, updates the department, adds history + audit log.

## Validation Rules

- Reference numbers are always generated and validated server-side.
- Required application fields: `service_id`, `fullName`, `email`, `address`.
- Only statuses in the canonical list are accepted.
- Client-side checks are UX only; the backend re-validates everything.