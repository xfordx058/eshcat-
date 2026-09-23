"""Application model helpers.

Data access for applications is centralized in
services/application_service.py. This module documents the canonical
application fields used across the API for clarity.

Columns (database/schema.sql):
    id, reference_number, service_id, department_id,
    full_name, email, mobile, address, form_data (JSON),
    status, created_at, updated_at
"""

APPLICATION_STATUSES = [
    "Submitted",
    "Received",
    "Under Review",
    "Additional Requirements",
    "For Verification",
    "Forwarded",
    "Approved",
    "Rejected",
    "Ready for Release",
    "Completed",
    "Cancelled",
]