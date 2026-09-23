"""Staff model helpers.

Staff authentication and authorization are handled in
routes/staff.py using werkzeug password hashing. Staff passwords are
never stored in plain text.

Columns (database/schema.sql):
    id, name, email, password_hash, role, department_id, is_active, created_at
"""

STAFF_ROLES = ["Staff", "Department Head", "Administrator", "Auditor"]