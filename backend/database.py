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


def ensure_runtime_schema() -> None:
    """Create small runtime tables/migrations for databases made by older builds."""
    conn = get_connection()
    try:
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
        conn.commit()
    finally:
        conn.close()
    _ensure_columns()
    _seed_emergency_defaults()


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
