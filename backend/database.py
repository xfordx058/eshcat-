import os
import sqlite3

from . import config


def get_db_path() -> str:
    path = config.DATABASE_PATH
    if not os.path.isabs(path):
        path = os.path.join(os.path.dirname(os.path.abspath(__file__)), path)
    directory = os.path.dirname(path)
    if directory and not os.path.exists(directory):
        os.makedirs(directory)
    return path


def get_connection() -> sqlite3.Connection:
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