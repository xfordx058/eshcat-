import os
import sqlite3

from flask import Flask, jsonify
from flask_cors import CORS

from . import config, database
from .routes.announcements import announcements_bp
from .routes.applications import applications_bp
from .routes.appointments import appointments_bp
from .routes.reports import reports_bp
from .routes.services import public_bp
from .routes.staff import staff_bp
from .routes.tracking import tracking_bp


def create_app() -> Flask:
    app = Flask(
        __name__,
        static_folder=os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "frontend"),
        static_url_path="",
    )
    app.config["SECRET_KEY"] = config.SECRET_KEY
    app.config["SESSION_PERMANENT"] = True
    app.config["SESSION_COOKIE_HTTPONLY"] = True
    app.config["SESSION_COOKIE_SAMESITE"] = "Lax"

    CORS(app, supports_credentials=True)

    @app.route("/", endpoint="home")
    def index():
        return app.send_static_file("index.html")

    from werkzeug.exceptions import HTTPException

    @app.errorhandler(HTTPException)
    def handle_http_exception(exc: HTTPException):
        response = jsonify({"error": exc.description if exc.code and exc.code < 500 else "Something went wrong."})
        response.status_code = exc.code or 500
        return response

    @app.errorhandler(sqlite3.Error)
    def handle_db_error(exc):
        return jsonify({"error": "Database error."}), 500

    register_blueprints(app)

    return app


def register_blueprints(app: Flask) -> None:
    app.register_blueprint(public_bp)
    app.register_blueprint(applications_bp)
    app.register_blueprint(tracking_bp)
    app.register_blueprint(appointments_bp)
    app.register_blueprint(reports_bp)
    app.register_blueprint(announcements_bp)
    app.register_blueprint(staff_bp)


app = create_app()


@app.cli.command("init-db")
def init_db_command():
    database.init_db()
    print("Database initialized.")


@app.cli.command("seed-db")
def seed_db_command():
    database.seed_db()
    print("Database seeded.")


@app.cli.command("flush-queue")
def flush_queue_command():
    from .services.email_service import flush_email_queue

    reply = flush_email_queue()
    print(f"Email queue flushed: {reply}")


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5000, debug=False)
