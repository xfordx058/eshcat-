import json
import os
import sys
import tempfile


def main():
    tmpdir = tempfile.mkdtemp()
    os.environ["DATABASE_PATH"] = os.path.join(tmpdir, "test.sqlite")
    sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

    from backend.app import create_app
    from backend import database

    database.init_db()
    database.seed_db()

    app = create_app()
    app.config.update(TESTING=True, SECRET_KEY="test")
    client = app.test_client()

    # 1. Public: list services
    r = client.get("/api/services")
    assert r.status_code == 200, r.status_code
    services = r.get_json()
    assert len(services) >= 6, len(services)
    print("OK  GET /api/services ->", len(services), "services")

    # 2. Public: single service with form fields
    r = client.get("/api/services/1")
    assert r.status_code == 200
    svc = r.get_json()
    assert svc["form_fields"], "no form fields"
    assert svc["requirements"]
    print("OK  GET /api/services/1 ->", svc["name"], f"({len(svc['form_fields'])} fields)")

    # 3. Public: submit application
    payload = {
        "service_id": 1,
        "fullName": "Test Applicant",
        "email": "test@example.com",
        "mobile": "09170000000",
        "address": "Catarman",
        "form_data": {
            "deceasedName": "John Doe",
            "dateOfDeath": "2026-01-01",
            "relationship": "Spouse",
            "purpose": "Legal affairs",
            "contactMethod": "Email",
        },
    }
    r = client.post("/api/applications", json=payload)
    assert r.status_code == 201, (r.status_code, r.get_data(as_text=True))
    ref = r.get_json()["reference_number"]
    assert ref.startswith("CAT-")
    print("OK  POST /api/applications ->", ref)

    # 4. Public: track
    r = client.get(f"/api/track/{ref}")
    assert r.status_code == 200
    tracked = r.get_json()
    assert tracked["status"] == "Submitted"
    assert tracked["timeline"]
    print("OK  GET /api/track ->", tracked["status"])

    # 5. Staff: login
    r = client.post("/api/staff/login", json={"email": "staff@eshcat.local", "password": "change_me_123"})
    assert r.status_code == 200, (r.status_code, r.get_data(as_text=True))
    me = r.get_json()
    print("OK  POST /api/staff/login ->", me["name"], f"({me['role']})")

    # 6. Staff: dashboard
    r = client.get("/api/staff/dashboard")
    assert r.status_code == 200
    stats = r.get_json()
    print("OK  GET /api/staff/dashboard ->", stats)

    # 7. Staff: application list
    r = client.get("/api/staff/applications")
    assert r.status_code == 200
    apps = r.get_json()
    assert len(apps) >= 2
    print("OK  GET /api/staff/applications ->", len(apps), "apps")

    # 8. Staff: get submitted application id
    submitted = None
    for a in apps:
        if a["reference_number"] == ref:
            submitted = a
            break
    assert submitted is not None, "new app not found"
    r = client.get(f"/api/staff/applications/{submitted['id']}")
    assert r.status_code == 200
    detail = r.get_json()
    assert detail["form_data"]
    print("OK  GET /api/staff/applications/{id}")

    # 9. Staff: update status
    r = client.patch(f"/api/staff/applications/{submitted['id']}/status", json={"status": "Under Review", "remarks": "Verified details."})
    assert r.status_code == 200, (r.status_code, r.get_data(as_text=True))
    print("OK  PATCH status -> Under Review")

    # 10. Public track reflects new status
    r = client.get(f"/api/track/{ref}")
    assert r.get_json()["status"] == "Under Review"
    print("OK  track reflects updated status")

    # 11. Staff: forward
    r = client.post(f"/api/staff/applications/{submitted['id']}/forward", json={"department_id": 4, "remarks": "Route to Engineering"})
    assert r.status_code == 200, (r.status_code, r.get_data(as_text=True))
    print("OK  POST forward")

    # 12. Staff: unauthorized guard
    client2 = app.test_client()
    r = client2.get("/api/staff/dashboard")
    assert r.status_code == 401
    print("OK  unauthenticated -> 401")

    # 13. Allowed statuses endpoint consumed app: announcements
    r = client.get("/api/announcements")
    assert r.status_code == 200 and len(r.get_json()) >= 3
    print("OK  GET /api/announcements ->", len(r.get_json()))

    # 14. appointments
    r = client.post("/api/appointments", json={"department_id": 1, "service_id": 1, "date": "2026-10-01", "time": "10:00 AM", "full_name": "A Test", "email": "a@example.com", "mobile": "0917"})
    assert r.status_code == 201
    print("OK  POST /api/appointments ->", r.get_json()["reference_number"])

    # 15. reports
    r = client.post("/api/reports", json={"category": "Road problem", "location": "Rizal St.", "description": "Deep pothole.", "name": "A Test", "email": "a@example.com"})
    assert r.status_code == 201
    print("OK  POST /api/reports ->", r.get_json()["reference_number"])

    # 16. offices directory
    r = client.get("/api/offices")
    assert r.status_code == 200
    offices = r.get_json()
    assert len(offices) >= 6
    assert "services" in offices[0]
    print("OK  GET /api/offices ->", len(offices), "offices")

    print("\nALL CHECKS PASSED")


if __name__ == "__main__":
    main()