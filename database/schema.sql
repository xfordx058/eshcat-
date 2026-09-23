-- eSHCAT Database Schema
-- Electronic Services Hub for Catarman

PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS departments (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT NOT NULL,
    description TEXT,
    location    TEXT,
    contact_number TEXT,
    email       TEXT,
    office_hours TEXT,
    created_at  TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS services (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    department_id   INTEGER NOT NULL REFERENCES departments(id),
    name            TEXT NOT NULL,
    short_description TEXT,
    description     TEXT,
    estimated_processing TEXT,
    is_online       INTEGER NOT NULL DEFAULT 1,
    created_at      TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at      TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS service_requirements (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    service_id  INTEGER NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    requirement TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS service_form_fields (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    service_id  INTEGER NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    label       TEXT NOT NULL,
    field_name  TEXT NOT NULL,
    field_type  TEXT NOT NULL DEFAULT 'text',
    required    INTEGER NOT NULL DEFAULT 1,
    options     TEXT
);

CREATE TABLE IF NOT EXISTS staff_users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT NOT NULL,
    email         TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    role          TEXT NOT NULL DEFAULT 'Staff',
    department_id INTEGER REFERENCES departments(id),
    is_active     INTEGER NOT NULL DEFAULT 1,
    created_at    TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS civil_users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    username      TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    full_name     TEXT NOT NULL,
    email         TEXT,
    created_at    TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS applications (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    reference_number TEXT NOT NULL UNIQUE,
    service_id       INTEGER NOT NULL REFERENCES services(id),
    department_id    INTEGER NOT NULL REFERENCES departments(id),
    full_name        TEXT NOT NULL,
    email            TEXT NOT NULL,
    mobile           TEXT,
    address          TEXT,
    form_data        TEXT NOT NULL DEFAULT '{}',
    status           TEXT NOT NULL DEFAULT 'Submitted',
    created_at       TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at       TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS application_history (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    application_id INTEGER NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    staff_id       INTEGER REFERENCES staff_users(id),
    old_status     TEXT,
    new_status     TEXT,
    remarks        TEXT,
    created_at     TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS appointments (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    reference_number TEXT NOT NULL UNIQUE,
    department_id    INTEGER REFERENCES departments(id),
    service_id       INTEGER REFERENCES services(id),
    appointment_date TEXT,
    appointment_time TEXT,
    full_name        TEXT NOT NULL,
    email            TEXT NOT NULL,
    mobile           TEXT,
    status           TEXT NOT NULL DEFAULT 'Pending',
    user_id          INTEGER REFERENCES civil_users(id),
    remarks          TEXT,
    created_at       TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at       TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS appointment_history (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    appointment_id INTEGER NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
    staff_id       INTEGER REFERENCES staff_users(id),
    old_status     TEXT,
    new_status     TEXT,
    remarks        TEXT,
    created_at     TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS community_reports (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    reference_number TEXT NOT NULL UNIQUE,
    department_id    INTEGER REFERENCES departments(id),
    category         TEXT NOT NULL,
    location         TEXT,
    description      TEXT NOT NULL,
    name             TEXT,
    email            TEXT,
    photo_data       TEXT,
    status           TEXT NOT NULL DEFAULT 'Open',
    user_id          INTEGER REFERENCES civil_users(id),
    remarks          TEXT,
    created_at       TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at       TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS announcements (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    title       TEXT NOT NULL,
    date_text   TEXT,
    department  TEXT,
    description TEXT,
    is_pinned   INTEGER NOT NULL DEFAULT 0,
    created_at  TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS emergency_numbers (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    label       TEXT NOT NULL,
    value       TEXT NOT NULL,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    updated_by  INTEGER REFERENCES staff_users(id),
    updated_at  TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS emergency_incidents (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    reference_number TEXT NOT NULL UNIQUE,
    severity        TEXT NOT NULL CHECK (severity IN ('Yellow', 'Orange', 'Red')),
    category        TEXT NOT NULL,
    location        TEXT,
    latitude        REAL,
    longitude       REAL,
    accuracy_meters REAL,
    description     TEXT,
    status          TEXT NOT NULL DEFAULT 'Open' CHECK (status IN ('Open', 'Responding', 'Resolved')),
    responder_id    INTEGER REFERENCES staff_users(id),
    resolved_by     INTEGER REFERENCES staff_users(id),
    followup_token_hash TEXT,
    created_at      TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at      TEXT NOT NULL DEFAULT (datetime('now')),
    resolved_at     TEXT
);

CREATE TABLE IF NOT EXISTS emergency_incident_actions (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    incident_id INTEGER NOT NULL REFERENCES emergency_incidents(id) ON DELETE CASCADE,
    staff_id    INTEGER NOT NULL REFERENCES staff_users(id),
    action      TEXT NOT NULL CHECK (action IN ('Responded', 'Resolved', 'Ignored')),
    created_at  TEXT NOT NULL DEFAULT (datetime('now')),
    UNIQUE (incident_id, staff_id, action)
);

CREATE TABLE IF NOT EXISTS agency_directory (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT NOT NULL,
    department  TEXT,
    location    TEXT,
    contact     TEXT,
    email       TEXT,
    office_hours TEXT,
    services    TEXT
);

CREATE TABLE IF NOT EXISTS notifications (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    application_id INTEGER REFERENCES applications(id),
    recipient_email TEXT NOT NULL,
    subject        TEXT NOT NULL,
    message        TEXT NOT NULL,
    status         TEXT NOT NULL DEFAULT 'Pending',
    retry_count    INTEGER NOT NULL DEFAULT 0,
    last_error     TEXT,
    created_at     TEXT NOT NULL DEFAULT (datetime('now')),
    sent_at        TEXT
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    staff_id    INTEGER REFERENCES staff_users(id),
    action      TEXT NOT NULL,
    entity_type TEXT,
    entity_id   INTEGER,
    details     TEXT,
    created_at  TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS sync_queue (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    payload     TEXT NOT NULL,
    endpoint_path TEXT,
    created_at  TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS comelec_settings (
    id                    INTEGER PRIMARY KEY CHECK (id = 1),
    election_day_active   INTEGER NOT NULL DEFAULT 0,
    public_lookup_enabled INTEGER NOT NULL DEFAULT 0,
    election_date        TEXT,
    queue_room            TEXT NOT NULL DEFAULT 'COMELEC Room 1',
    announcement          TEXT,
    updated_by            INTEGER REFERENCES staff_users(id),
    updated_at            TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS comelec_queue (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    queue_date     TEXT NOT NULL,
    present_number INTEGER NOT NULL,
    full_name      TEXT NOT NULL,
    email          TEXT,
    precinct_number TEXT,
    purpose        TEXT NOT NULL DEFAULT 'Voter verification',
    room           TEXT NOT NULL,
    status         TEXT NOT NULL DEFAULT 'Waiting',
    created_at     TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at     TEXT NOT NULL DEFAULT (datetime('now')),
    UNIQUE(queue_date, present_number)
);
