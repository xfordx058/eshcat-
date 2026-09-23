import json
import subprocess
import sys
import time
import urllib.request

# Start server
proc = subprocess.Popen(
    [sys.executable, "backend/app.py"],
    cwd=r"C:\Projects\eshcat",
    stdout=subprocess.DEVNULL,
    stderr=subprocess.DEVNULL,
)
time.sleep(4)
try:
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor())

    req = urllib.request.Request(
        "http://127.0.0.1:5000/api/staff/login",
        data=json.dumps({
            "email": "staff@eshcat.local",
            "password": "change_me_123",
        }).encode(),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    resp = opener.open(req)
    print("LOGIN:", resp.status, resp.read().decode())

    resp = opener.open("http://127.0.0.1:5000/api/staff/dashboard")
    print("DASHBOARD:", resp.status, resp.read().decode())
finally:
    proc.terminate()