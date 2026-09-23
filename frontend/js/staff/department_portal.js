(function () {
  const api = window.ESH.api;
  const qs = (s) => document.querySelector(s);
  let departmentId = null;
  let departmentName = "Department";
  let tabs = {};

  function ensureShell() {
    if (document.getElementById("portalTable")) return;
    document.getElementById("portalRoot").innerHTML = `
      <div class="dp-shell">
        <aside class="dp-sidebar"><div class="dp-brand"><span class="mark">eS</span><span><strong>eSHCAT</strong><small>Department Portal</small></span></div>
          <nav class="dp-nav"><span class="nav-section">BPLO Workspace</span><a class="active" href="#">Overview</a><a href="#applications" data-portal-link="applications">Applications</a><a href="#appointments" data-portal-link="appointments">Appointments</a><a href="#reports" data-portal-link="reports">Reports</a></nav>
          <div class="dp-user"><strong id="departmentUser">Staff</strong><small id="departmentName">Loading department...</small></div>
        </aside>
        <main class="dp-main"><div class="dp-topbar"><div><h1>Department Portal</h1><p>Applications and requests assigned to your department.</p></div><div class="dp-actions"><button class="dp-btn" id="refreshPortal" type="button">Refresh</button><button class="dp-btn primary" data-logout type="button">Sign out</button></div></div>
          <div class="dp-stats"><div class="dp-stat"><div class="num" id="servicesCount">0</div><div class="label">Services</div></div><div class="dp-stat"><div class="num" id="applicationsCount">0</div><div class="label">Applications</div></div><div class="dp-stat"><div class="num" id="appointmentsCount">0</div><div class="label">Appointments</div></div><div class="dp-stat"><div class="num" id="reportsCount">0</div><div class="label">Reports</div></div></div>
          <section class="dp-card"><h2>Department Work Queue</h2><p>Review records routed to this office.</p><div class="dp-toolbar"><button class="dp-btn active" data-portal-tab="applications" type="button">Applications</button><button class="dp-btn" data-portal-tab="appointments" type="button">Appointments</button><button class="dp-btn" data-portal-tab="reports" type="button">Reports</button></div><div id="portalTable"><div class="dp-loading">Loading department data...</div></div></section>
        </main>
      </div>`;
  }

  const esc = (v) => String(v == null ? "—" : v).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
  const fmtDate = (v) => v ? new Date(String(v).replace(" ", "T")).toLocaleDateString() : "—";

  function table(rows, columns) {
    if (!rows.length) return '<div class="dp-empty">No records found for this department.</div>';
    return '<div class="dp-table-wrap"><table class="dp-table"><thead><tr>' + columns.map((c) => `<th>${c.label}</th>`).join("") + '</tr></thead><tbody>' + rows.map((row) => `<tr>${columns.map((c) => `<td>${c.render ? c.render(row) : esc(row[c.key])}</td>`).join("")}</tr>`).join("") + '</tbody></table></div>';
  }

  function render(type) {
    const rows = tabs[type] || [];
    const columns = type === "applications"
      ? [{ key: "reference_number", label: "Reference" }, { key: "service_name", label: "Service" }, { key: "full_name", label: "Applicant" }, { key: "status", label: "Status", render: (r) => `<span class="dp-badge">${esc(r.status)}</span>` }, { key: "created_at", label: "Submitted", render: (r) => fmtDate(r.created_at) }]
      : type === "appointments"
        ? [{ key: "reference_number", label: "Reference" }, { key: "full_name", label: "Applicant" }, { key: "appointment_date", label: "Date", render: (r) => fmtDate(r.appointment_date) }, { key: "appointment_time", label: "Time" }, { key: "status", label: "Status", render: (r) => `<span class="dp-badge">${esc(r.status)}</span>` }]
        : [{ key: "reference_number", label: "Reference" }, { key: "category", label: "Category" }, { key: "location", label: "Location" }, { key: "description", label: "Concern" }, { key: "status", label: "Status", render: (r) => `<span class="dp-badge">${esc(r.status)}</span>` }];
    qs("#portalTable").innerHTML = table(rows, columns);
    document.querySelectorAll("[data-portal-tab]").forEach((b) => b.classList.toggle("active", b.dataset.portalTab === type));
  }

  async function init() {
    ensureShell();
    const me = await api.get("/staff/me");
    const expectedDepartment = Number(document.body.dataset.portalDepartment || 0);
    if (expectedDepartment && me.role === "Administrator") {
      departmentId = expectedDepartment;
    } else {
      departmentId = me.department_id;
    }
    if (expectedDepartment && Number(departmentId) !== expectedDepartment) {
      qs("#portalTable").innerHTML = "<div class=\"dp-empty\">This portal belongs to a different department.</div>";
      return;
    }
    const services = await api.get("/services");
    const mine = services.filter((s) => Number(s.department_id) === Number(departmentId));
    departmentName = mine[0]?.department || `Department ${departmentId || "Portal"}`;
    qs("#departmentName").textContent = departmentName;
    qs(".dp-topbar h1").textContent = departmentName;
    qs("#departmentUser").textContent = me.name;
    const query = `department_id=${encodeURIComponent(departmentId)}`;
    const [applications, appointments, reports] = await Promise.all([
      api.get(`/staff/applications?${query}`), api.get(`/staff/appointments?${query}`), api.get(`/staff/reports?${query}`),
    ]);
    tabs = { applications, appointments, reports };
    qs("#applicationsCount").textContent = applications.length;
    qs("#appointmentsCount").textContent = appointments.length;
    qs("#reportsCount").textContent = reports.length;
    qs("#servicesCount").textContent = mine.length;
    render("applications");
    document.querySelectorAll("[data-portal-tab]").forEach((button) => button.addEventListener("click", () => render(button.dataset.portalTab)));
    document.querySelectorAll("[data-portal-link]").forEach((link) => link.addEventListener("click", (event) => {
      event.preventDefault();
      render(link.dataset.portalLink);
    }));
    qs("#refreshPortal").addEventListener("click", () => window.location.reload());
  }

  document.addEventListener("DOMContentLoaded", () => init().catch((err) => { qs("#portalTable").innerHTML = `<div class="dp-empty">${esc(err.message)}</div>`; }));
})();
