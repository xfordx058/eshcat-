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

  function destination(user) {
    // The general staff account keeps the main dashboard. Department-specific
    // accounts use the portal assigned to their department.
    if (user.role === "Administrator" || (user.role === "Staff" && !String(user.email || "").startsWith("staff."))) {
      return "/pages/staff/dashboard.html";
    }
    if (Number(user.department_id) === 1) return "/pages/staff/civil_portal.html";
    const portals = {
      2: "bplo_portal.html",
      3: "treasurer_portal.html",
      4: "building_portal.html",
      5: "assessor_portal.html",
      6: "planning_portal.html",
      7: "mayor_portal.html",
      8: "health_portal.html",
      9: "social_welfare_portal.html",
      10: "environment_portal.html",
      11: "comelec_portal.html",
    };
    return `/pages/staff/${portals[Number(user.department_id)] || "department_portal.html"}`;
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
        const user = await ESH.api.post("/staff/login", payload);
        window.location.href = destination(user);
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
          <span class="user-chip-text" style="min-width:0">
            <span class="u-name" style="display:block">${ESH.esc(user.name)}</span>
            <span class="u-role" style="display:block">${ESH.esc(user.role)}</span>
          </span>
        `;
        chip.style.display = "flex";
        chip.style.alignItems = "center";
        chip.style.gap = "10px";
        chip.style.minWidth = "0";
      }
      const greet = document.getElementById("greetName");
      if (greet) {
        const hour = new Date().getHours();
        const period = hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";
        greet.textContent = `${period}, ${user.name.split(" ")[0] || "there"}`;
      }
      const dateEl = document.getElementById("todayDate");
      if (dateEl) {
        dateEl.textContent = new Date().toLocaleDateString(undefined, { weekday: "long", month: "long", day: "numeric", year: "numeric" });
      }
    });
  }

  document.addEventListener("DOMContentLoaded", () => {
    if (document.body.dataset.page === "staff-login") initLogin();
    initLogout();
    initStaffCheck();
  });
})();
