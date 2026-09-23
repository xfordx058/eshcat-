import os

from dotenv import load_dotenv

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

load_dotenv(os.path.join(BASE_DIR, ".env"))

DATABASE_PATH = os.getenv(
    "DATABASE_PATH",
    os.path.join(os.path.dirname(BASE_DIR), "database", "eshcat.sqlite"),
)

DB_DRIVER = os.getenv("DB_DRIVER", "sqlite").lower()
MYSQL_HOST = os.getenv("MYSQL_HOST", "127.0.0.1")
MYSQL_PORT = int(os.getenv("MYSQL_PORT", "3306"))
MYSQL_DATABASE = os.getenv("MYSQL_DATABASE", "eshcat")
MYSQL_USER = os.getenv("MYSQL_USER", "root")
MYSQL_PASSWORD = os.getenv("MYSQL_PASSWORD", "")

SECRET_KEY = os.getenv("SECRET_KEY", "change-this-secret-key-in-production")

STAFF_SEED_PASSWORD = os.getenv("STAFF_SEED_PASSWORD", "change_me_123")

SMTP_HOST = os.getenv("SMTP_HOST", "smtp.gmail.com")
SMTP_PORT = int(os.getenv("SMTP_PORT", "465"))
SMTP_SECURITY = os.getenv("SMTP_SECURITY", "SSL")
SMTP_USERNAME = os.getenv("SMTP_USERNAME", "")
SMTP_PASSWORD = os.getenv("SMTP_PASSWORD", "")
MAIL_FROM = os.getenv("MAIL_FROM", "eSHCAT <no-reply@eshcat.local>")

EMAIL_ENABLED = bool(SMTP_USERNAME and SMTP_PASSWORD)
