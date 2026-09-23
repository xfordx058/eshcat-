(function () {
  const ESH = window.ESH;
  const api = window.ESH.api;
  const root = document.getElementById("comelecRoot");
  const statuses = ["Submitted", "Under Review", "Additional Requirements", "For Verification", "Approved", "Rejected", "Not Found", "Ready for Release", "Completed", "Forwarded"];
  let state = { applications: [], queue: [], settings: {}, user: null };
  const esc = (value) => String(value == null ? "—" : value).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
  const fmt = (value) => value ? new Date(String(value).replace(" ", "T")).toLocaleString(undefined, { month: "short", day: "numeric", hour: "numeric", minute: "2-digit" }) : "—";

  function shell() {
    root.innerHTML = `<div class="dp-shell"><aside class="dp-sidebar"><div class="dp-brand"><span class="mark">eS</span><span><strong>eSHCAT</strong><small>COMELEC Portal</small></span></div><nav class="dp-nav"><span class="nav-section">COMELEC Workspace</span><a class="active" href="#applications" data-tab="applications">Applications</a><a href="#queue" data-tab="queue">Election-day Queue</a><a href="#settings" data-tab="settings">Settings</a>${state.user && state.user.role === "Administrator" ? '<a href="#staff" data-tab="staff">Staff</a>' : ""}</nav><div class="dp-user"><strong>${esc(state.user?.name || "Staff")}</strong><small>Commission on Elections</small><button class="dp-btn" data-logout type="button" style="margin-top:12px;width:100%">Sign out</button></div></aside><main class="dp-main"><div class="dp-topbar"><div><span class="comelec-kicker">Department workspace</span><h1>COMELEC Dashboard</h1><p>Manage voter certification requests and election-day assistance.</p></div><div class="dp-actions"><span class="comelec-live" id="electionState">Election day inactive</span><button class="dp-btn" id="refreshComelec" type="button">Refresh</button></div></div><div id="comelecContent"><div class="dp-loading">Loading COMELEC dashboard...</div></div></main></div>`;
    root.querySelectorAll("[data-tab]").forEach((link) => link.addEventListener("click", (event) => { event.preventDefault(); root.querySelectorAll("[data-tab]").forEach((item) => item.classList.toggle("active", item === link)); render(link.dataset.tab); }));
    root.querySelector("[data-logout]")?.addEventListener("click", async () => { await api.post("/staff/logout", {}); window.location.href = "/pages/staff/login.html"; });
    root.querySelector("#refreshComelec").addEventListener("click", load);
  }

  function stat(label, value, tone) { return `<div class="comelec-stat ${tone}"><strong>${value}</strong><span>${label}</span></div>`; }

  function applicationTable() {
    const rows = state.applications;
    return `<div class="comelec-toolbar"><input id="applicationSearch" type="search" placeholder="Search name or reference number" aria-label="Search applications"><select id="applicationServiceFilter" aria-label="Filter transaction"><option value="">All transactions</option><option>Voter's Certification</option><option>Transfer of Voter Registration</option></select><select id="applicationFilter" aria-label="Filter application status"><option value="">All statuses</option>${statuses.map((s) => `<option>${s}</option>`).join("")}</select></div><div id="applicationRows">${rows.length ? `<div class="dp-table-wrap"><table class="dp-table"><thead><tr><th>Reference</th><th>Transaction</th><th>Applicant</th><th>Submitted</th><th>Status</th><th>Action</th></tr></thead><tbody>${rows.map((row) => `<tr data-app-id="${row.id}"><td><strong>${esc(row.reference_number)}</strong></td><td>${esc(row.service_name)}</td><td>${esc(row.full_name)}<small>${esc(row.email)}</small></td><td>${fmt(row.created_at)}</td><td><span class="dp-badge status-${esc((row.status || "").toLowerCase().replaceAll(" ", "-"))}">${esc(row.status)}</span></td><td><a class="dp-btn dp-btn-small" href="comelec_application.html?id=${encodeURIComponent(row.id)}">View</a></td></tr>`).join("")}</tbody></table></div>` : '<div class="dp-empty">No COMELEC applications found.</div>'}</div>`;
  }

  function renderApplications() {
    const c = document.getElementById("comelecContent");
    const count = (status) => state.applications.filter((row) => row.status === status).length;
    c.innerHTML = `<div class="comelec-stats">${stat("Pending", count("Submitted"), "pending")}${stat("In Progress", count("Under Review") + count("Additional Requirements") + count("For Verification"), "progress")}${stat("Rejected", count("Rejected"), "rejected")}${stat("Not Found", count("Not Found"), "not-found")}${stat("Done", count("Completed"), "done")}</div><section class="dp-card"><div class="comelec-section-heading"><div><h2>Voter Certification Applications</h2><p>Review requests, update status, and keep a complete application history.</p></div><span class="comelec-count">${state.applications.length} total</span></div>${applicationTable()}</section>`;
    const refreshRows = () => {
      const q = (document.getElementById("applicationSearch").value || "").toLowerCase();
      const serviceFilter = document.getElementById("applicationServiceFilter").value;
      const filter = document.getElementById("applicationFilter").value;
      document.querySelectorAll("#applicationRows tbody tr").forEach((tr) => {
        const row = state.applications.find((item) => String(item.id) === tr.dataset.appId);
        const match = row && (!q || `${row.full_name} ${row.reference_number} ${row.email}`.toLowerCase().includes(q)) && (!serviceFilter || row.service_name === serviceFilter) && (!filter || row.status === filter);
        tr.hidden = !match;
      });
    };
    document.getElementById("applicationSearch")?.addEventListener("input", refreshRows);
    document.getElementById("applicationServiceFilter")?.addEventListener("change", refreshRows);
    document.getElementById("applicationFilter")?.addEventListener("change", refreshRows);
  }

  function renderQueue() {
    const settings = state.settings;
    const rows = state.queue;
    document.getElementById("comelecContent").innerHTML = `<div class="comelec-queue-banner ${settings.election_day_active ? "active" : ""}"><div><span class="comelec-kicker">Public queue control</span><h2>${settings.election_day_active ? "Election-day queue is active" : "Election-day queue is inactive"}</h2><p>${esc(settings.announcement || "Turn on the queue when COMELEC is ready to receive voters.")}</p></div><strong>${esc(settings.queue_room || "COMELEC Room 1")}</strong></div><section class="dp-card"><div class="comelec-section-heading"><div><h2>Today’s Present Numbers</h2><p>Monitor voters waiting for verification and direct them to the assigned room.</p></div><span class="comelec-count">${rows.length} issued today</span></div>${rows.length ? `<div class="dp-table-wrap"><table class="dp-table"><thead><tr><th>Present No.</th><th>Voter</th><th>Precinct</th><th>Purpose</th><th>Status</th><th>Update</th></tr></thead><tbody>${rows.map((row) => `<tr><td><strong class="present-number">${row.present_number}</strong></td><td>${esc(row.full_name)}<small>${esc(row.email || "No email")}</small></td><td>${esc(row.precinct_number)}</td><td>${esc(row.purpose)}</td><td><span class="dp-badge">${esc(row.status)}</span></td><td><select class="queue-status" data-queue-id="${row.id}">${["Waiting", "Serving", "Done", "Cancelled"].map((status) => `<option${row.status === status ? " selected" : ""}>${status}</option>`).join("")}</select></td></tr>`).join("")}</tbody></table></div>` : '<div class="dp-empty">No present numbers issued today.</div>'}</section>`;
    document.querySelectorAll(".queue-status").forEach((select) => select.addEventListener("change", async () => { try { await api.patch(`/comelec/queue/${select.dataset.queueId}`, { status: select.value }); await load(); } catch (err) { alert(err.message); } }));
  }

  function renderSettings() {
    const s = state.settings;
    document.getElementById("comelecContent").innerHTML = `<section class="dp-card"><div class="comelec-section-heading"><div><h2>Election-day Controls</h2><p>Enable voter queue intake and public present-number lookup only when COMELEC is ready.</p></div></div><div class="comelec-settings-grid"><label class="comelec-toggle"><input id="electionActive" type="checkbox"${s.election_day_active ? " checked" : ""}><span><strong>Enable election-day queue</strong><small>Residents can request a present number.</small></span></label><label class="comelec-toggle"><input id="lookupEnabled" type="checkbox"${s.public_lookup_enabled ? " checked" : ""}><span><strong>Enable present-number search</strong><small>Residents can check their queue status.</small></span></label><label>Election date<input id="electionDate" type="date" value="${esc(s.election_date || "")}"></label><label>Assigned room<input id="queueRoom" value="${esc(s.queue_room || "COMELEC Room 1")}"></label><label class="full">Public announcement<textarea id="comelecAnnouncement" rows="3">${esc(s.announcement || "")}</textarea></label></div><div class="comelec-settings-actions"><button class="dp-btn primary" id="saveComelecSettings" type="button">Save COMELEC Settings</button><button class="dp-btn" id="createDemoQueue" type="button">Create 100 Demo People</button></div><p class="note">Demo data is clearly labelled and intended for presentations/testing only.</p></section>`;
    document.getElementById("saveComelecSettings").addEventListener("click", async () => { const button = document.getElementById("saveComelecSettings"); button.disabled = true; try { await api.patch("/comelec/settings", { election_day_active: document.getElementById("electionActive").checked, public_lookup_enabled: document.getElementById("lookupEnabled").checked, election_date: document.getElementById("electionDate").value, queue_room: document.getElementById("queueRoom").value.trim(), announcement: document.getElementById("comelecAnnouncement").value.trim() }); await load(); } catch (err) { alert(err.message); } finally { button.disabled = false; } });
    document.getElementById("createDemoQueue").addEventListener("click", async () => { if (!confirm("Create 100 demo people in today’s COMELEC queue?")) return; try { const result = await api.post("/comelec/demo-queue", {}); alert(result.message); await load(); } catch (err) { alert(err.message); } });
  }

  function renderStaff() { document.getElementById("comelecContent").innerHTML = staffPanel(); loadStaff(); }

  function staffPanel() { return `<section class="dp-card" style="margin-top:18px"><div class="comelec-section-heading"><div><h2>COMELEC Staff Management</h2><p>Search current COMELEC staff or add a staff account assigned to this department.</p></div><button class="dp-btn primary" id="addComelecStaff" type="button">Add Staff</button></div><input id="staffSearch" class="comelec-wide-input" type="search" placeholder="Search COMELEC staff"><div id="comelecStaffRows"><div class="dp-loading">Loading staff...</div></div></section>`; }

  async function loadStaff() {
    if (!document.getElementById("comelecStaffRows")) return;
    try { const users = await api.get("/staff/users?department_id=11"); const draw = () => { const q = (document.getElementById("staffSearch").value || "").toLowerCase(); const rows = users.filter((u) => !q || `${u.name} ${u.email} ${u.role}`.toLowerCase().includes(q)); document.getElementById("comelecStaffRows").innerHTML = rows.length ? `<div class="dp-table-wrap"><table class="dp-table"><thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Status</th></tr></thead><tbody>${rows.map((u) => `<tr><td>${esc(u.name)}</td><td>${esc(u.email)}</td><td>${esc(u.role)}</td><td><span class="dp-badge">${u.is_active ? "Active" : "Inactive"}</span></td></tr>`).join("")}</tbody></table></div>` : '<div class="dp-empty">No COMELEC staff found.</div>'; }; draw(); document.getElementById("staffSearch").addEventListener("input", draw); document.getElementById("addComelecStaff").addEventListener("click", addStaff); } catch (err) { document.getElementById("comelecStaffRows").innerHTML = `<div class="dp-empty">${esc(err.message)}</div>`; }
  }

  async function addStaff() {
    const name = prompt("Staff full name:"); const email = prompt("Staff email:"); const password = prompt("Temporary password (at least 6 characters):");
    if (!name || !email || !password) return;
    try { await api.post("/staff/users", { name, email, password, role: "Staff", department_id: 11 }); alert("COMELEC staff account created."); render("staff"); } catch (err) { alert(err.message); }
  }

  function openApplication(row) {
    if (!row) return;
    const modal = ESH.openModal(`<div class="comelec-detail-grid"><div><span>Reference</span><strong>${esc(row.reference_number)}</strong></div><div><span>Applicant</span><strong>${esc(row.full_name)}</strong></div><div><span>Email</span><strong>${esc(row.email)}</strong></div><div><span>Last updated</span><strong>${fmt(row.updated_at)}</strong></div></div><div id="applicationDetailBody" class="dp-loading">Loading application history...</div>`, { title: "Voter Certification Application" });
    api.get(`/staff/applications/${row.id}`).then((detail) => { const form = Object.entries(detail.form_data || {}).filter(([key]) => !["fullName", "email", "mobile", "address"].includes(key)).map(([key, value]) => `<div><span>${esc(key.replaceAll(/([A-Z])/g, " $1"))}</span><strong>${esc(value)}</strong></div>`).join(""); const history = (detail.history || []).map((item) => `<li><strong>${esc(item.new_status)}</strong><small>${fmt(item.created_at)} · ${esc(item.staff_name || "System")}</small><p>${esc(item.remarks || "No remarks")}</p></li>`).join(""); modal.el("#applicationDetailBody").innerHTML = `<h3>Application Details</h3><div class="comelec-detail-grid">${form || "<div>No additional details.</div>"}</div><h3>Application Status</h3><div class="comelec-quick-actions"><button class="dp-btn" data-quick-status="Approved" type="button">Approve</button><button class="dp-btn" data-quick-status="Rejected" type="button">Reject</button><button class="dp-btn" data-quick-status="Additional Requirements" type="button">Request Requirements</button></div><div class="comelec-update"><select id="detailStatus">${statuses.map((s) => `<option${s === detail.status ? " selected" : ""}>${s}</option>`).join("")}</select><textarea id="detailRemarks" rows="2" placeholder="Add staff remarks for the requester"></textarea><button class="dp-btn primary" id="saveApplicationStatus" type="button">Update Status</button></div><h3>Application History</h3><ol class="comelec-history">${history || "<li>No history yet.</li>"}</ol>`; modal.el("#saveApplicationStatus").addEventListener("click", async () => { try { await api.patch(`/staff/applications/${row.id}/status`, { status: modal.el("#detailStatus").value, remarks: modal.el("#detailRemarks").value.trim() }); modal.close(); await load(); } catch (err) { alert(err.message); } }); modal.modal.querySelectorAll("[data-quick-status]").forEach((button) => button.addEventListener("click", () => { modal.el("#detailStatus").value = button.dataset.quickStatus; modal.el("#saveApplicationStatus").click(); })); }).catch((err) => { modal.el("#applicationDetailBody").textContent = err.message; });
  }

  async function load() { try { state = { ...state, ...(await api.get("/comelec/dashboard")) }; document.getElementById("electionState").textContent = state.settings.election_day_active ? "Election day active" : "Election day inactive"; render(document.querySelector("[data-tab].active")?.dataset.tab || "applications"); } catch (err) { document.getElementById("comelecContent").innerHTML = `<div class="dp-empty">${esc(err.message)}</div>`; } }
  function render(tab) { if (tab === "queue") renderQueue(); else if (tab === "settings") renderSettings(); else if (tab === "staff") renderStaff(); else renderApplications(); }
  document.addEventListener("DOMContentLoaded", async () => { try { state.user = await api.get("/staff/me"); shell(); await load(); } catch (err) { root.innerHTML = `<div class="dp-empty">${esc(err.message)}</div>`; } });
})();
