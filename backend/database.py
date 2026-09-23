import os
import sqlite3

from . import config


class MySQLConnection:
    """Small compatibility wrapper for the existing SQLite-style data access."""

    def __init__(self):
        try:
            import mysql.connector
        except ImportError as exc:
            raise RuntimeError(
                "MySQL support requires mysql-connector-python. Install backend/requirements.txt."
            ) from exc
        self._connection = mysql.connector.connect(
            host=config.MYSQL_HOST,
            port=config.MYSQL_PORT,
            database=config.MYSQL_DATABASE,
            user=config.MYSQL_USER,
            password=config.MYSQL_PASSWORD,
        )

    def execute(self, sql, params=()):
        cursor = self._connection.cursor(dictionary=True)
        cursor.execute(sql.replace("?", "%s"), params)
        return cursor

    def executemany(self, sql, params):
        cursor = self._connection.cursor(dictionary=True)
        cursor.executemany(sql.replace("?", "%s"), params)
        return cursor

    def commit(self):
        self._connection.commit()

    def rollback(self):
        self._connection.rollback()

    def close(self):
        self._connection.close()

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_value, traceback):
        if exc_type:
            self.rollback()
        else:
            self.commit()


def get_db_path() -> str:
    path = config.DATABASE_PATH
    if not os.path.isabs(path):
        path = os.path.join(os.path.dirname(os.path.abspath(__file__)), path)
    directory = os.path.dirname(path)
    if directory and not os.path.exists(directory):
        os.makedirs(directory)
    return path


def get_connection() -> sqlite3.Connection:
    if config.DB_DRIVER == "mysql":
        return MySQLConnection()
    conn = sqlite3.connect(get_db_path())
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys = ON")
    return conn


def init_db() -> None:
    schema_path = os.path.join(
        os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
        "database",
        "schema.sql",
    )
    conn = get_connection()
    with conn:
        conn.executescript(open(schema_path, encoding="utf-8").read())
    _ensure_columns()
    _seed_emergency_defaults()


# Columns added to previously-created databases (create-table for fresh installs
# lives in schema.sql). Keyed by table name on purpose so a fresh schema.sql is
# the single source of truth going forward.
_CIVIL_MIGRATIONS = [
    ("appointments", "user_id", "user_id INTEGER"),
    ("appointments", "remarks", "remarks TEXT"),
    ("appointments", "updated_at", "updated_at TEXT DEFAULT (datetime('now'))"),
    ("community_reports", "department_id", "department_id INTEGER"),
    ("community_reports", "user_id", "user_id INTEGER"),
    ("community_reports", "remarks", "remarks TEXT"),
    ("community_reports", "photo_data", "photo_data TEXT"),
    ("community_reports", "updated_at", "updated_at TEXT DEFAULT (datetime('now'))"),
    ("emergency_incidents", "followup_token_hash", "followup_token_hash TEXT"),
    ("emergency_incidents", "accuracy_meters", "accuracy_meters REAL"),
]


def _ensure_columns() -> None:
    conn = get_connection()
    try:
        with conn:
            for table, column, ddl in _CIVIL_MIGRATIONS:
                if config.DB_DRIVER == "mysql":
                    existing = [
                        r["COLUMN_NAME"]
                        for r in conn.execute(
                            """
                            SELECT COLUMN_NAME FROM information_schema.columns
                            WHERE table_schema = DATABASE() AND table_name = ?
                            """,
                            (table,),
                        )
                    ]
                else:
                    existing = [r["name"] for r in conn.execute(f"PRAGMA table_info({table})")]
                if column not in existing:
                    mysql_ddl = {
                        "user_id": "user_id INTEGER",
                        "remarks": "remarks TEXT",
                        "updated_at": "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
                        "accuracy_meters": "accuracy_meters DOUBLE NULL",
                    }.get(column, ddl)
                    sqlite_ddl = {
                        "updated_at": "updated_at TEXT",
                    }.get(column, ddl)
                    conn.execute(f"ALTER TABLE {table} ADD COLUMN {mysql_ddl if config.DB_DRIVER == 'mysql' else sqlite_ddl}")
            if config.DB_DRIVER == "mysql":
                conn.execute(
                    """CREATE TABLE IF NOT EXISTS appointment_history (
                        id INT AUTO_INCREMENT PRIMARY KEY, appointment_id INT NOT NULL,
                        staff_id INT NULL, old_status VARCHAR(100), new_status VARCHAR(100),
                        remarks TEXT, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )"""
                )
            else:
                conn.execute(
                    """CREATE TABLE IF NOT EXISTS appointment_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT, appointment_id INTEGER NOT NULL,
                        staff_id INTEGER, old_status TEXT, new_status TEXT, remarks TEXT,
                        created_at TEXT NOT NULL DEFAULT (datetime('now'))
                    )"""
                )
    finally:
        conn.close()


# Shown on the public homepage "Emergency & Important Numbers" card. Staff can
# edit them; defaults are only inserted when the table is empty so staff edits
# are never overwritten.
_EMERGENCY_DEFAULTS = [
    ("MDRRMO", "0906-357-0985"),
    ("Municipal Hall", "(055) 500-0712"),
    ("Rural Health Unit", "(055) 500-9615"),
    ("Emergency Hotline", "Dial 911 / 112"),
]


def _seed_emergency_defaults() -> None:
    conn = get_connection()
    try:
        with conn:
            empty = conn.execute("SELECT COUNT(*) AS c FROM emergency_numbers").fetchone()["c"] == 0
            if empty:
                conn.executemany(
                    "INSERT INTO emergency_numbers (label, value, sort_order) VALUES (?, ?, ?)",
                    [(label, value, i) for i, (label, value) in enumerate(_EMERGENCY_DEFAULTS)],
                )
    finally:
        conn.close()


def _seed_comelec_defaults() -> None:
    """Add the COMELEC office/service to older databases without overwriting edits."""
    conn = get_connection()
    try:
        with conn:
            if conn.execute("SELECT COUNT(*) AS c FROM departments").fetchone()["c"] == 0 or conn.execute("SELECT COUNT(*) AS c FROM services").fetchone()["c"] == 0:
                return
            department = conn.execute("SELECT id FROM departments WHERE name = ?", ("Commission on Elections (COMELEC)",)).fetchone()
            if department is None:
                cursor = conn.execute(
                    """INSERT INTO departments
                       (name, description, location, contact_number, email, office_hours)
                       VALUES (?, ?, ?, ?, ?, ?)""",
                    ("Commission on Elections (COMELEC)", "Handles voter registration, voter certifications, and election-day assistance.", "Municipal Hall, Catarman, Northern Samar", "(055) 500-0712", "comelec.catarman@gmail.com", "Election day and office hours vary"),
                )
                department_id = cursor.lastrowid
            else:
                department_id = department["id"]

            service = conn.execute("SELECT id FROM services WHERE department_id = ? AND name = ?", (department_id, "Voter's Certification")).fetchone()
            if service is None:
                service = conn.execute(
                    """INSERT INTO services
                       (department_id, name, short_description, description, estimated_processing, is_online)
                       VALUES (?, ?, ?, ?, ?, 1)""",
                    (department_id, "Voter's Certification", "Request a certification of voter registration.", "Submit your information for COMELEC verification and receive a reference number for tracking.", "3-5 working days"),
                )
                service_id = service.lastrowid
                requirements = ["Valid government-issued ID", "Voter registration details or precinct number", "Correct full name and date of birth"]
                for requirement in requirements:
                    conn.execute("INSERT INTO service_requirements (service_id, requirement) VALUES (?, ?)", (service_id, requirement))
                fields = [
                    ("Full Name", "fullName", "text", 1, None),
                    ("Email Address", "email", "email", 1, None),
                    ("Mobile Number", "mobile", "tel", 0, None),
                    ("Complete Address", "address", "text", 1, None),
                    ("Date of Birth", "dateOfBirth", "date", 1, None),
                    ("Precinct Number", "precinctNumber", "text", 1, None),
                    ("Barangay", "barangay", "text", 1, None),
                    ("Valid Government-Issued ID", "validIdType", "select", 1, '["National ID / PhilSys ID","Driver\'s License","Philippine Passport","UMID / SSS / GSIS ID","Voter\'s ID","Other Government-Issued ID"]'),
                    ("Purpose of Certification", "purpose", "select", 1, '["Employment","School","Government transaction","Personal record","Other"]'),
                ]
                conn.executemany(
                    "INSERT INTO service_form_fields (service_id, label, field_name, field_type, required, options) VALUES (?, ?, ?, ?, ?, ?)",
                    [(service_id, *field) for field in fields],
                )
            transfer = conn.execute("SELECT id FROM services WHERE department_id = ? AND name = ?", (department_id, "Transfer of Voter Registration")).fetchone()
            if transfer is None:
                transfer_id = conn.execute(
                    """INSERT INTO services
                       (department_id, name, short_description, description, estimated_processing, is_online)
                       VALUES (?, ?, ?, ?, ?, 1)""",
                    (department_id, "Transfer of Voter Registration", "Request transfer to a new voting precinct or barangay.", "Submit your current and new registration details for COMELEC verification and transfer processing.", "7-10 working days"),
                ).lastrowid
                transfer_requirements = ["Valid government-issued ID", "Current voter registration record or Voter's ID", "Proof of new residence or barangay certification", "Previous precinct or barangay information", "Authorization letter if filed by a representative"]
                conn.executemany("INSERT INTO service_requirements (service_id, requirement) VALUES (?, ?)", [(transfer_id, item) for item in transfer_requirements])
                transfer_fields = [
                    ("Full Name", "fullName", "text", 1, None), ("Email Address", "email", "email", 1, None),
                    ("Mobile Number", "mobile", "tel", 0, None), ("Current Address", "address", "text", 1, None),
                    ("Date of Birth", "dateOfBirth", "date", 1, None), ("Current Precinct Number", "currentPrecinct", "text", 1, None),
                    ("Current Barangay", "currentBarangay", "text", 1, None), ("New Address", "newAddress", "text", 1, None),
                    ("New Barangay", "newBarangay", "text", 1, None), ("Valid Government-Issued ID", "validIdType", "select", 1, '["National ID / PhilSys ID","Driver\'s License","Philippine Passport","UMID / SSS / GSIS ID","Voter\'s ID","Other Government-Issued ID"]'),
                    ("Filing as", "filingAs", "select", 1, '["Registered voter","Authorized representative"]'),
                ]
                conn.executemany("INSERT INTO service_form_fields (service_id, label, field_name, field_type, required, options) VALUES (?, ?, ?, ?, ?, ?)", [(transfer_id, *field) for field in transfer_fields])
            if conn.execute("SELECT id FROM comelec_settings WHERE id = 1").fetchone() is None:
                conn.execute("INSERT INTO comelec_settings (id, election_day_active, public_lookup_enabled, queue_room, announcement) VALUES (1, 0, 0, ?, ?)", ("COMELEC Room 1", "Election-day queue is currently inactive."))
            if conn.execute("SELECT id FROM staff_users WHERE email = ?", ("staff.comelec@eshcat.local",)).fetchone() is None:
                from werkzeug.security import generate_password_hash
                conn.execute(
                    "INSERT INTO staff_users (name, email, password_hash, role, department_id, is_active) VALUES (?, ?, ?, 'Staff', ?, 1)",
                    ("COMELEC Service Staff", "staff.comelec@eshcat.local", generate_password_hash(config.STAFF_SEED_PASSWORD), department_id),
                )
            if conn.execute("SELECT id FROM staff_users WHERE email = ?", ("head.comelec@eshcat.local",)).fetchone() is None:
                from werkzeug.security import generate_password_hash
                conn.execute(
                    "INSERT INTO staff_users (name, email, password_hash, role, department_id, is_active) VALUES (?, ?, ?, 'Department Head', ?, 1)",
                    ("COMELEC Department Head", "head.comelec@eshcat.local", generate_password_hash(config.STAFF_SEED_PASSWORD), department_id),
                )
    finally:
        conn.close()


def ensure_runtime_schema() -> None:
    """Create small runtime tables/migrations for databases made by older builds."""
    conn = get_connection()
    try:
        if config.DB_DRIVER == "mysql":
            conn.execute(
                """CREATE TABLE IF NOT EXISTS emergency_incidents (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    reference_number VARCHAR(40) NOT NULL UNIQUE,
                    severity VARCHAR(20) NOT NULL,
                    category VARCHAR(100) NOT NULL,
                    location TEXT,
                    latitude DOUBLE NULL,
                    longitude DOUBLE NULL,
                    accuracy_meters DOUBLE NULL,
                    description TEXT,
                    status VARCHAR(20) NOT NULL DEFAULT 'Open',
                    responder_id INT NULL,
                    resolved_by INT NULL,
                    followup_token_hash TEXT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    resolved_at TIMESTAMP NULL
                )"""
            )
        else:
            conn.execute(
                """CREATE TABLE IF NOT EXISTS emergency_incidents (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    reference_number TEXT NOT NULL UNIQUE,
                    severity TEXT NOT NULL CHECK (severity IN ('Yellow', 'Orange', 'Red')),
                    category TEXT NOT NULL,
                    location TEXT,
                    latitude REAL,
                    longitude REAL,
                    accuracy_meters REAL,
                    description TEXT,
                    status TEXT NOT NULL DEFAULT 'Open' CHECK (status IN ('Open', 'Responding', 'Resolved')),
                    responder_id INTEGER REFERENCES staff_users(id),
                    resolved_by INTEGER REFERENCES staff_users(id),
                    followup_token_hash TEXT,
                    created_at TEXT NOT NULL DEFAULT (datetime('now')),
                    updated_at TEXT NOT NULL DEFAULT (datetime('now')),
                    resolved_at TEXT
                )"""
            )
        if config.DB_DRIVER != "mysql":
            has_core_tables = conn.execute(
                "SELECT COUNT(*) AS c FROM sqlite_master WHERE type = 'table' AND name IN ('appointments', 'civil_users', 'staff_users')"
            ).fetchone()["c"] == 3
            if not has_core_tables:
                schema_path = os.path.join(
                    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                    "database", "schema.sql"
                )
                conn.executescript(open(schema_path, encoding="utf-8").read())
        if config.DB_DRIVER == "mysql":
            conn.execute(
                """
                CREATE TABLE IF NOT EXISTS emergency_numbers (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    label VARCHAR(255) NOT NULL,
                    value VARCHAR(255) NOT NULL,
                    sort_order INT NOT NULL DEFAULT 0,
                    updated_by INT NULL,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """
            )
            conn.execute(
                """CREATE TABLE IF NOT EXISTS emergency_incident_actions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    incident_id INT NOT NULL,
                    staff_id INT NOT NULL,
                    action VARCHAR(20) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY emergency_action_once (incident_id, staff_id, action)
                )"""
            )
        else:
            conn.execute(
                """
                CREATE TABLE IF NOT EXISTS emergency_numbers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    label TEXT NOT NULL,
                    value TEXT NOT NULL,
                    sort_order INTEGER NOT NULL DEFAULT 0,
                    updated_by INTEGER,
                    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
                )
                """
            )
            conn.execute(
                """CREATE TABLE IF NOT EXISTS emergency_incident_actions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    incident_id INTEGER NOT NULL REFERENCES emergency_incidents(id) ON DELETE CASCADE,
                    staff_id INTEGER NOT NULL REFERENCES staff_users(id),
                    action TEXT NOT NULL CHECK (action IN ('Responded', 'Resolved', 'Ignored')),
                    created_at TEXT NOT NULL DEFAULT (datetime('now')),
                    UNIQUE (incident_id, staff_id, action)
                )"""
            )
        if config.DB_DRIVER == "mysql":
            conn.execute(
                """CREATE TABLE IF NOT EXISTS comelec_settings (
                    id INT PRIMARY KEY, election_day_active TINYINT NOT NULL DEFAULT 0,
                    public_lookup_enabled TINYINT NOT NULL DEFAULT 0, election_date DATE NULL,
                    queue_room VARCHAR(255) NOT NULL DEFAULT 'COMELEC Room 1', announcement TEXT,
                    updated_by INT NULL, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )"""
            )
            conn.execute(
                """CREATE TABLE IF NOT EXISTS comelec_queue (
                    id INT AUTO_INCREMENT PRIMARY KEY, queue_date DATE NOT NULL,
                    present_number INT NOT NULL, full_name VARCHAR(255) NOT NULL, email VARCHAR(255),
                    precinct_number VARCHAR(100), purpose VARCHAR(255) NOT NULL DEFAULT 'Voter verification',
                    room VARCHAR(255) NOT NULL, status VARCHAR(50) NOT NULL DEFAULT 'Waiting',
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY comelec_queue_number (queue_date, present_number)
                )"""
            )
        else:
            conn.execute(
                """CREATE TABLE IF NOT EXISTS comelec_settings (
                    id INTEGER PRIMARY KEY CHECK (id = 1), election_day_active INTEGER NOT NULL DEFAULT 0,
                    public_lookup_enabled INTEGER NOT NULL DEFAULT 0, election_date TEXT,
                    queue_room TEXT NOT NULL DEFAULT 'COMELEC Room 1', announcement TEXT,
                    updated_by INTEGER, updated_at TEXT NOT NULL DEFAULT (datetime('now'))
                )"""
            )
            conn.execute(
                """CREATE TABLE IF NOT EXISTS comelec_queue (
                    id INTEGER PRIMARY KEY AUTOINCREMENT, queue_date TEXT NOT NULL,
                    present_number INTEGER NOT NULL, full_name TEXT NOT NULL, email TEXT,
                    precinct_number TEXT, purpose TEXT NOT NULL DEFAULT 'Voter verification',
                    room TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'Waiting',
                    created_at TEXT NOT NULL DEFAULT (datetime('now')), updated_at TEXT NOT NULL DEFAULT (datetime('now')),
                    UNIQUE(queue_date, present_number)
                )"""
            )
        conn.commit()
    finally:
        conn.close()
    _ensure_columns()
    _seed_emergency_defaults()
    _seed_comelec_defaults()
    _seed_drrmo_defaults()


def _seed_drrmo_defaults() -> None:
    """Ensure a DRRMO department and demo staff accounts exist."""
    from werkzeug.security import generate_password_hash

    conn = get_connection()
    try:
        with conn:
            if conn.execute("SELECT COUNT(*) AS c FROM departments").fetchone()["c"] == 0:
                return
            department = conn.execute(
                "SELECT id FROM departments WHERE name = ?", ("Municipal Disaster Risk Reduction and Management Office (MDRRMO)",)
            ).fetchone()
            if department is None:
                cursor = conn.execute(
                    """INSERT INTO departments (name, description, location, contact_number, email, office_hours)
                       VALUES (?, ?, ?, ?, ?, ?)""",
                    ("Municipal Disaster Risk Reduction and Management Office (MDRRMO)",
                     "Coordinates emergency response and disaster risk reduction for Catarman.",
                     "Municipal Hall, Catarman, Northern Samar", "0906-357-0985", None, "Contact MDRRMO for response availability"),
                )
                department_id = cursor.lastrowid
            else:
                department_id = department["id"]
            for name, email, role in (
                ("MDRRMO Response Staff", "staff.drrmo@eshcat.local", "Staff"),
                ("MDRRMO Department Head", "head.drrmo@eshcat.local", "Department Head"),
            ):
                if conn.execute("SELECT id FROM staff_users WHERE email = ?", (email,)).fetchone() is None:
                    conn.execute(
                        "INSERT INTO staff_users (name, email, password_hash, role, department_id, is_active) VALUES (?, ?, ?, ?, ?, 1)",
                        (name, email, generate_password_hash(config.STAFF_SEED_PASSWORD), role, department_id),
                    )
    finally:
        conn.close()


def seed_db() -> None:
    from werkzeug.security import generate_password_hash

    from . import config

    seed_path = os.path.join(
        os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
        "database",
        "seed.sql",
    )
    conn = get_connection()
    with conn:
        conn.executescript(open(seed_path, encoding="utf-8").read())
        password = generate_password_hash(config.STAFF_SEED_PASSWORD)
        conn.execute(
            "UPDATE staff_users SET password_hash = ? WHERE password_hash = 'SEED_ME'",
            (password,),
        )
        conn.execute(
            "UPDATE civil_users SET password_hash = ? WHERE password_hash = 'SEED_ME'",
            (password,),
        )
    _seed_drrmo_defaults()


def log_audit(conn: sqlite3.Connection, staff_id, action, entity_type=None, entity_id=None, details=None) -> None:
    conn.execute(
        "INSERT INTO audit_logs (staff_id, action, entity_type, entity_id, details) VALUES (?, ?, ?, ?, ?)",
        (staff_id, action, entity_type, entity_id, str(details) if details else None),
    )


def make_ref(prefix: str, length: int = 8) -> str:
    import secrets
    import string

    alphabet = string.ascii_uppercase + string.digits
    return f"{prefix}-{''.join(secrets.choice(alphabet) for _ in range(length))}"
