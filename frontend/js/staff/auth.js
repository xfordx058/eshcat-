/* eSHCAT — staff/auth.js
   Staff login, session check, logout. */

(function () {
  const ESH = window.ESH;

  async function me() {
    try {
      return await ESH.api.get("/staff/me");
    } catch {
      return null;
    }
  }

  function initLogin() {
    const form = document.getElementById("loginForm");
    if (!form) return;
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      const btn = document.getElementById("loginBtn");
      const payload = {
        email: document.getElementById("loginEmail").value,
        password: document.getElementById("loginPassword").value,
      };
      if (!payload.email || !payload.password) {
        ESH.showToast("Email and password are required.", "error");
        return;
      }
      ESH.setLoading(btn, "Signing in...");
      try {
        await ESH.api.post("/staff/login", payload);
        window.location.href = "/pages/staff/dashboard.html";
      } catch (err) {
        ESH.unsetLoading(btn);
        ESH.showToast(err.message || "Login failed.", "error");
      }
    });
  }

  function initLogout() {
    document.querySelectorAll("[data-logout]").forEach((btn) => {
      btn.addEventListener("click", async () => {
        try {
          await ESH.api.post("/staff/logout", {});
        } catch {
          /* ignore */
        }
        window.location.href = "/pages/staff/login.html";
      });
    });
  }

  function initStaffCheck() {
    // Pages that require auth will include data-require-auth on body
    const body = document.body;
    if (body.dataset.requireAuth === undefined) return;

    me().then((user) => {
      if (!user) {
        window.location.href = "/pages/staff/login.html";
        return;
      }
      document.body.classList.add("staff-authed");
      const chip = document.getElementById("userChip");
      if (chip) {
        const initials = (user.name || "?").split(/\s+/).map((x) => x[0]).slice(0, 2).join("").toUpperCase();
        chip.innerHTML = `
          <span class="avatar" aria-hidden="true">${ESH.esc(initials)}</span>
          <span class="user-chip-text">
            <span class="name">${ESH.esc(user.name)}</span>
            <span class="role">${ESH.esc(user.role)}</span>
          </span>
        `;
      }
    });
  }

  document.addEventListener("DOMContentLoaded", () => {
    if (document.body.dataset.page === "staff-login") initLogin();
    initLogout();
    initStaffCheck();
  });
})();