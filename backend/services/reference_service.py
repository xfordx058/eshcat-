import secrets
import string

from ..database import get_connection

ALPHABET = string.ascii_uppercase + string.digits


def _random(prefix: str) -> str:
    return f"{prefix}-{''.join(secrets.choice(ALPHABET) for _ in range(8))}"


def generate_reference(prefix: str = "CAT") -> str:
    """Generate a unique reference number, verified against existing tables."""
    conn = get_connection()
    try:
        while True:
            candidate = _random(prefix)
            row = conn.execute(
                "SELECT 1 FROM applications WHERE reference_number = ? "
                "UNION SELECT 1 FROM appointments WHERE reference_number = ? "
                "UNION SELECT 1 FROM community_reports WHERE reference_number = ? LIMIT 1",
                (candidate, candidate, candidate),
            ).fetchone()
            if row is None:
                return candidate
    finally:
        conn.close()