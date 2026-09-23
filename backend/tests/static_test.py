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

    # Static file serving
    checks = {
        "/": 200,
        "/index.html": 200,
        "/css/style.css": 200,
        "/css/components.css": 200,
        "/css/responsive.css": 200,
        "/css/staff.css": 200,
        "/js/utils.js": 200,
        "/js/api.js": 200,
        "/js/app.js": 200,
        "/js/services.js": 200,
        "/js/applications.js": 200,
        "/js/tracking.js": 200,
        "/js/announcements.js": 200,
        "/js/appointments.js": 200,
        "/js/reports.js": 200,
        "/js/offices.js": 200,
        "/js/emergency.js": 200,
        "/js/staff/auth.js": 200,
        "/js/staff/dashboard.js": 200,
        "/js/staff/applications.js": 200,
        "/js/comelec.js": 200,
        "/js/staff/comelec_portal.js": 200,
        "/js/staff/comelec_application.js": 200,
        "/css/comelec.css": 200,
        "/css/comelec_application.css": 200,
        "/pages/services.html": 200,
        "/pages/service-details.html": 200,
        "/pages/apply.html": 200,
        "/pages/track.html": 200,
        "/pages/appointments.html": 200,
        "/pages/reports.html": 200,
        "/pages/announcements.html": 200,
        "/pages/offices.html": 200,
        "/pages/about.html": 200,
        "/pages/comelec.html": 200,
        "/pages/staff/login.html": 200,
        "/pages/staff/dashboard.html": 200,
        "/pages/staff/applications.html": 200,
        "/pages/staff/application-details.html": 200,
        "/pages/staff/civil_portal.html": 200,
        "/pages/staff/comelec_portal.html": 200,
        "/pages/staff/comelec_application.html": 200,
        "/css/civil_portal.css": 200,
        "/css/fontawesome-fallback.css": 200,
        "/manifest.json": 200,
        "/service-worker.js": 200,
        "/assets/logo/favicon.svg": 200,
        "/assets/logo/icon-192.svg": 200,
        "/assets/fontawesome/css/all.min.css": 200,
        "/assets/fontawesome/webfonts/fa-solid-900.woff2": 200,
        "/assets/fontawesome/webfonts/fa-regular-400.woff2": 200,
        "/assets/fontawesome/webfonts/fa-brands-400.woff2": 200,
    }
    failed = 0
    for path, expected in checks.items():
        r = client.get(path)
        status = "OK " if r.status_code == expected else "FAIL"
        if r.status_code != expected:
            failed += 1
        print(f"{status} {path} -> {r.status_code}")

    # Content sanity
    r = client.get("/")
    assert b"Electronic Services Hub" in r.data, "index.html missing hero text"
    r = client.get("/pages/services.html")
    assert b"Municipal Services" in r.data, "services.html missing"
    print("OK  Home page and pages contain expected content.")

    if failed:
        print(f"\n{failed} static checks FAILED")
        sys.exit(1)
    print("\nSTATIC SERVING CHECKS PASSED")


if __name__ == "__main__":
    main()
