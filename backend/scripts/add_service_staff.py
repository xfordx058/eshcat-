"""Create one staff login for every department in the database.

Run from the project root:
    python -m backend.scripts.add_service_staff

The command is safe to run repeatedly. Existing users are left unchanged.
"""

from werkzeug.security import generate_password_hash

from .. import config
from ..database import get_connection


STAFF = {
    1: ("LCRO Service Staff", "staff.lcro@eshcat.local"),
    2: ("BPLO Service Staff", "staff.bplo@eshcat.local"),
    3: ("MTO Service Staff", "staff.mto@eshcat.local"),
    4: ("OBO Service Staff", "staff.obo@eshcat.local"),
    5: ("MASSO Service Staff", "staff.masso@eshcat.local"),
    6: ("MPDO Service Staff", "staff.mpdo@eshcat.local"),
    7: ("Mayor Office Staff", "staff.mayor@eshcat.local"),
    8: ("MHO Service Staff", "staff.mho@eshcat.local"),
    9: ("MSWDO Service Staff", "staff.mswdo@eshcat.local"),
    10: ("MENRO Service Staff", "staff.menro@eshcat.local"),
}


def main():
    conn = get_connection()
    password_hash = generate_password_hash(config.STAFF_SEED_PASSWORD)
    created = 0
    skipped = 0
    try:
        for department_id, (name, email) in STAFF.items():
            department = conn.execute(
                "SELECT id FROM departments WHERE id = ?", (department_id,)
            ).fetchone()
            if department is None:
                print(f"SKIP {email}: department {department_id} does not exist")
                skipped += 1
                continue

            existing = conn.execute(
                "SELECT id FROM staff_users WHERE email = ?", (email,)
            ).fetchone()
            if existing:
                print(f"EXISTS {email}")
                skipped += 1
                continue

            conn.execute(
                """
                INSERT INTO staff_users
                    (name, email, password_hash, role, department_id, is_active)
                VALUES (?, ?, ?, 'Staff', ?, 1)
                """,
                (name, email, password_hash, department_id),
            )
            print(f"ADDED {email} -> department {department_id}")
            created += 1
        conn.commit()
    finally:
        conn.close()
    print(f"Created: {created}; skipped: {skipped}")
    print(f"Password: value of STAFF_SEED_PASSWORD ({config.STAFF_SEED_PASSWORD})")


if __name__ == "__main__":
    main()
