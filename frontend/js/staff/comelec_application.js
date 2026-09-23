(function () {
  const ESH = window.ESH;
  const api = ESH.api;
  const root = document.getElementById("comelecApplicationRoot");
  const statuses = ["Submitted", "Under Review", "Additional Requirements", "For Verification", "Approved", "Rejected", "Not Found", "Ready for Release", "Completed", "Forwarded"];
  const esc = (value) => ESH.esc(value == null ? "—" : value);
  const fmt = (value) => value ? ESH.fmtDateTime(value) : "—";
  let application;

  function detailItems(items) {
    return `<div class="comelec-detail-grid">${items.map(([label, value]) => `<div><span>${esc(label)}</span><strong>${esc(value || "—")}</strong></div>`).join("")}</div>`;
  }

  function render() {
    const formData = Object.entries(application.form_data || {}).map(([key, value]) => [key.replace(/([A-Z])/g, " $1").trim(), value]);
    const history = (application.history || []).map((item) => `<li class="${item.new_status === application.status ? "current" : ""}"><strong>${esc(item.new_status || item.old_status)}</strong><small>${fmt(item.created_at)} · ${esc(item.staff_name || "System")}</small><p>${esc(item.remarks || "No remarks")}</p></li>`).join("");
    root.innerHTML = `<div class="comelec-page-shell"><aside class="staff-sidebar"><div class="side-brand"><span class="mark">eS</span><span><strong>eSHCAT</strong><small>COMELEC Portal</small></span></div><nav class="side-nav"><span class="nav-section">COMELEC Workspace</span><a class="active" href="comelec_portal.html">Applications</a><a href="comelec_portal.html#queue">Election-day Queue</a><a href="comelec_portal.html#settings">Settings</a></nav><div class="side-user"><div><span class="u-name">COMELEC Staff</span><span class="u-role">Application review</span></div><button class="side-user-logout" data-logout type="button" title="Sign out">↪</button></div></aside><main class="comelec-page-main"><div class="comelec-detail-header"><div><a class="comelec-back" href="comelec_portal.html">← Back to COMELEC Applications</a><span class="comelec-kicker">${esc(application.service_name)}</span><h1>${esc(application.reference_number)}</h1><p>Review the request, update its status, and keep the requester informed.</p></div><span class="badge">${esc(application.status)}</span></div><div class="comelec-detail-layout"><div><section class="card comelec-detail-card"><h2>Applicant Information</h2>${detailItems([["Full Name", application.full_name], ["Email", application.email], ["Mobile", application.mobile], ["Address", application.address], ["Submitted", fmt(application.created_at)], ["Last updated", fmt(application.updated_at)]])}</section><section class="card comelec-detail-card"><h2>Request Information</h2>${detailItems(formData)}</section><section class="card comelec-detail-card"><h2>Application History</h2><ol class="comelec-history">${history || "<li>No application history.</li>"}</ol></section></div><aside><section class="card comelec-detail-card comelec-action-card"><h2>Application Status</h2><p class="note">Last updated: ${fmt(application.updated_at)}</p><div class="status-actions"><button class="btn approve" data-quick-status="Approved" type="button">Approve</button><button class="btn reject" data-quick-status="Rejected" type="button">Reject</button></div><label>Status<select id="statusSelect">${statuses.map((status) => `<option${status === application.status ? " selected" : ""}>${status}</option>`).join("")}</select></label><label>Staff remarks<textarea id="statusRemarks" rows="4" placeholder="Remarks sent to the requester"></textarea></label><button class="btn btn-secondary" id="updateStatus" type="button">Update Status</button><button class="btn btn-secondary" id="requestRequirements" type="button">Request Requirements</button></section></aside></div></main></div>`;
    root.querySelector("[data-logout]").addEventListener("click", async () => { await api.post("/staff/logout", {}); window.location.href = "/pages/staff/login.html"; });
    root.querySelector("#updateStatus").addEventListener("click", updateStatus);
    root.querySelectorAll("[data-quick-status]").forEach((button) => button.addEventListener("click", () => { root.querySelector("#statusSelect").value = button.dataset.quickStatus; updateStatus(); }));
    root.querySelector("#requestRequirements").addEventListener("click", requestRequirements);
  }

  async function updateStatus() {
    const status = root.querySelector("#statusSelect").value;
    const remarks = root.querySelector("#statusRemarks").value.trim();
    if (status === application.status) { ESH.showToast(`No change made. This application is already ${status}.`, "error"); return; }
    const button = root.querySelector("#updateStatus");
    ESH.setLoading(button, "Saving...");
    try { await api.patch(`/staff/applications/${application.id}/status`, { status, remarks }); ESH.showToast("Status updated successfully.", "success"); await load(); }
    catch (err) { ESH.unsetLoading(button); ESH.showToast(err.message, "error"); }
  }

  async function requestRequirements() {
    let requirements = [];
    try { const service = await api.get(`/services/${application.service_id}`); requirements = service.requirements || []; } catch { /* keep fallback */ }
    const modal = ESH.openModal(`<p class="modal-sub">Select the documents the requester still needs to submit.</p><div class="comelec-requirements">${requirements.length ? requirements.map((item) => `<label><input class="requirement-item" type="checkbox"> <span>${esc(item)}</span></label>`).join("") : "<p class='note'>No requirements configured for this transaction.</p>"}</div><label>Message<textarea id="requirementsMessage" rows="3" placeholder="Please submit the missing documents."></textarea></label><div class="modal-actions"><button class="btn btn-secondary" data-close-modal type="button">Cancel</button><button class="btn btn-primary" id="sendRequirements" type="button">Send Request</button></div>`, { title: "Request Requirements" });
    modal.el("#sendRequirements").addEventListener("click", async () => { const selected = Array.from(modal.modal.querySelectorAll(".requirement-item:checked")).map((item) => item.parentElement.innerText.trim()); const message = ["Additional requirements requested:", ...selected, modal.el("#requirementsMessage").value.trim()].filter(Boolean).join("\n• "); try { await api.patch(`/staff/applications/${application.id}/status`, { status: "Additional Requirements", remarks: message }); modal.close(); ESH.showToast("Requirements request sent.", "success"); await load(); } catch (err) { ESH.showToast(err.message, "error"); } });
  }

  async function load() {
    try { application = await api.get(`/staff/applications/${encodeURIComponent(ESH.getParam("id"))}`); render(); }
    catch (err) { ESH.showError(root, err.message, load); }
  }
  document.addEventListener("DOMContentLoaded", load);
})();
