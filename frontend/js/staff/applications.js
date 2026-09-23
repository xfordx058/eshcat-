/* eSHCAT — staff/applications.js
   Staff application list (search/status/department filters) +
   application detail (status panel with modals for update status,
   request requirements, forward, approve, reject). */

(function () {
  const ESH = window.ESH;

  const STATUSES = [
    "Submitted", "Received", "Under Review", "Additional Requirements",
    "For Verification", "Forwarded", "Approved", "Rejected", "Not Found",
    "Ready for Release", "Completed", "Cancelled",
  ];

  async function initList() {
    const container = document.getElementById("applicationsTable");
    const searchEl = document.getElementById("filterSearch");
    const statusEl = document.getElementById("filterStatus");
    const deptEl = document.getElementById("filterDept");
    if (!container) return;

    let rows = [];
    let departments = [];

    try {
      const services = await ESH.api.get("/services");
      const map = new Map();
      services.forEach((s) => {
        if (!map.has(s.department_id)) map.set(s.department_id, s.department);
      });
      departments = [...map.values()];
      departments.forEach((name) => {
        deptEl.appendChild(ESH.el("option", { value: name, text: name }));
      });
    } catch {
      /* keep dropdown empty */
    }

    async function load() {
      const status = statusEl ? statusEl.value : "";
      const params = new URLSearchParams();
      if (status) params.set("status", status);
      ESH.showLoading(container, "Loading applications...");
      try {
        rows = await ESH.api.get(`/staff/applications?${params.toString()}`);
      } catch (err) {
        ESH.showError(container, err.message, load);
        return;
      }
      draw();
    }

    function draw() {
      const q = (searchEl ? searchEl.value : "").trim().toLowerCase();
      const dept = deptEl ? deptEl.value : "";
      const filtered = rows.filter((a) => {
        const matchQ =
          !q ||
          a.reference_number.toLowerCase().includes(q) ||
          (a.full_name || "").toLowerCase().includes(q) ||
          (a.service_name || "").toLowerCase().includes(q);
        const matchDept = !dept || a.department_name === dept;
        return matchQ && matchDept;
      });

      if (!filtered.length) {
        ESH.showEmpty(
          container,
          "No applications match your filters.",
          `<button class="btn btn-secondary" id="clearStaffFiltersBtn" type="button">Clear Filters</button>`
        );
        container.querySelector("#clearStaffFiltersBtn")?.addEventListener("click", () => {
          if (searchEl) searchEl.value = "";
          if (deptEl) deptEl.value = "";
          if (statusEl) statusEl.value = "";
          load();
        });
        return;
      }

      renderTable(container, filtered);
    }

    searchEl?.addEventListener("input", draw);
    deptEl?.addEventListener("change", draw);
    statusEl?.addEventListener("change", load);
    document.getElementById("refreshBtn")?.addEventListener("click", load);
    load();
  }

  function renderTable(container, rows) {
    container.innerHTML = "";
    const wrap = ESH.el("div", { class: "table-wrap" }, [
      ESH.el("table", { class: "data" }, [
        ESH.el("thead", {}, [ESH.el("tr", {}, [
          ESH.el("th", { text: "Reference" }),
          ESH.el("th", { text: "Service" }),
          ESH.el("th", { text: "Applicant" }),
          ESH.el("th", { text: "Department" }),
          ESH.el("th", { text: "Status" }),
          ESH.el("th", { text: "Submitted" }),
          ESH.el("th", {}, [ESH.el("span", { class: "sr-only", text: "Action" })]),
        ])]),
        ESH.el("tbody", {}, rows.map((a) =>
          ESH.el("tr", {}, [
            ESH.el("td", { "data-label": "Reference" }, [ESH.el("a", { href: `/pages/staff/application-details.html?id=${a.id}`, text: a.reference_number })]),
            ESH.el("td", { "data-label": "Service", text: a.service_name }),
            ESH.el("td", { "data-label": "Applicant", text: a.full_name }),
            ESH.el("td", { "data-label": "Department", text: a.department_name }),
            ESH.el("td", { "data-label": "Status" }, [ESH.statusBadge(a.status)]),
            ESH.el("td", { "data-label": "Submitted", text: ESH.fmtDate(a.created_at) }),
            ESH.el("td", { "data-label": "Action" }, [ESH.el("a", { class: "btn btn-sm btn-secondary", href: `/pages/staff/application-details.html?id=${a.id}`, text: "View" })]),
          ])
        )),
      ]),
    ]);
    container.appendChild(wrap);
  }

  async function initDetail() {
    const container = document.getElementById("applicationDetail");
    if (!container) return;
    ESH.showLoading(container, "Loading application...");

    const id = ESH.getParam("id");
    if (!id) {
      window.location.href = "/pages/staff/applications.html";
      return;
    }

    let app;
    try {
      app = await ESH.api.get(`/staff/applications/${id}`);
    } catch (err) {
      ESH.showError(container, err.message, () => initDetail());
      return;
    }

    container.innerHTML = "";

    const head = ESH.el("div", { style: "margin-bottom:20px" }, [
      ESH.el("a", { class: "note", href: "/pages/staff/applications.html" }, [ESH.el("i", { class: "fa-solid fa-arrow-left", "aria-hidden": "true" }), " Applications"]),
      ESH.el("div", { style: "display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-top:10px" }, [
        ESH.el("div", {}, [
          ESH.el("div", { class: "dept", text: app.department_name }),
          ESH.el("h1", { style: "margin:2px 0 4px;font-size:1.5rem", text: app.reference_number }),
          ESH.el("p", { class: "note", text: `${app.service_name} · submitted ${ESH.fmtDateTime(app.created_at)}` }),
        ]),
        ESH.statusBadge(app.status),
      ]),
    ]);
    container.appendChild(head);

    const split = ESH.el("div", { class: "split" });
    split.appendChild(
      ESH.el("div", {}, [
        applicantCard(app),
        formDataCard(app),
        timelineCard(app),
      ])
    );
    split.appendChild(actionsCard(app));
    container.appendChild(split);
  }

  function applicantCard(app) {
    const items = [
      ["Full Name", app.full_name],
      ["Email", app.email],
      ["Mobile", app.mobile || "—"],
      ["Address", app.address || "—"],
    ].filter(([, v]) => v);
    return ESH.el("div", { class: "card", style: "margin-bottom:16px" }, [
      ESH.el("h3", { text: "Applicant" }),
      ESH.el("div", { class: "detail-list" }, items.map(([k, v]) =>
        ESH.el("div", { class: "detail-item" }, [
          ESH.el("div", { class: "k", text: k }),
          ESH.el("div", { class: "v", text: v }),
        ])
      )),
    ]);
  }

  function formDataCard(app) {
    const entries = Object.entries(app.form_data || {});
    if (!entries.length) return null;
    return ESH.el("div", { class: "card", style: "margin-bottom:16px" }, [
      ESH.el("h3", { text: "Request Information" }),
      ESH.el("div", { class: "detail-list" }, entries.map(([k, v]) =>
        ESH.el("div", { class: "detail-item" }, [
          ESH.el("div", { class: "k", text: k.replace(/([A-Z])/g, " $1").trim() }),
          ESH.el("div", { class: "v", text: String(v ?? "") || "—" }),
        ])
      )),
    ]);
  }

  function timelineCard(app) {
    const entries = (app.history || []).slice();
    const ul = ESH.el("ul", { class: "timeline" });
    entries.forEach((h) => {
      const li = ESH.el("li", { class: h.new_status === app.status ? "current" : "done" }, [
        ESH.el("span", { class: "status-name", text: h.new_status || h.old_status }),
        ESH.el("div", { class: "time", text: `${ESH.fmtDateTime(h.created_at)} · ${h.staff_name || "System"}` }),
        h.remarks ? ESH.el("div", { class: "note", text: h.remarks }) : "",
      ]);
      ul.appendChild(li);
    });
    return ESH.el("div", { class: "card" }, [
      ESH.el("h3", { text: "Application History" }),
      ul,
    ]);
  }

  function actionsCard(app) {
    const card = ESH.el("div", { class: "card sticky-form" });
    card.appendChild(ESH.el("h3", { text: "Application Status" }));
    card.appendChild(ESH.el("div", { style: "margin:12px 0" }, [ESH.statusBadge(app.status)]));
    card.appendChild(ESH.el("p", { class: "note", text: "Last updated: " + ESH.fmtDateTime(app.updated_at) }));

    const approveBtn = ESH.el("button", { class: "btn approve", style: "flex:1", type: "button" }, [ESH.el("i", { class: "fa-solid fa-circle-check", "aria-hidden": "true" }), " Approve"]);
    const rejectBtn = ESH.el("button", { class: "btn reject", style: "flex:1", type: "button" }, [ESH.el("i", { class: "fa-solid fa-circle-xmark", "aria-hidden": "true" }), " Reject"]);
    card.appendChild(ESH.el("div", { class: "status-actions", style: "display:flex" }, [approveBtn, rejectBtn]));

    const updateBtn = ESH.el("button", { class: "btn btn-secondary", style: "width:100%;margin-top:8px", type: "button" }, [ESH.el("i", { class: "fa-solid fa-rotate", "aria-hidden": "true" }), " Update Status"]);
    const reqBtn = ESH.el("button", { class: "btn btn-secondary", style: "width:100%;margin-top:8px", type: "button" }, [ESH.el("i", { class: "fa-solid fa-clipboard-list", "aria-hidden": "true" }), " Request Requirements"]);
    const forwardBtn = ESH.el("button", { class: "btn btn-secondary", style: "width:100%;margin-top:8px", type: "button" }, [ESH.el("i", { class: "fa-solid fa-share", "aria-hidden": "true" }), " Forward to Department"]);
    card.appendChild(updateBtn);
    card.appendChild(reqBtn);
    card.appendChild(forwardBtn);

    card.appendChild(ESH.el("hr", { style: "border:none;border-top:1px solid var(--color-border);margin:18px 0" }));
    card.appendChild(ESH.el("h3", { text: "Application Details" }));
    card.appendChild(ESH.el("div", { class: "detail-list", style: "margin-top:12px" }, [
      ESH.el("div", { class: "detail-item" }, [ESH.el("div", { class: "k", text: "Reference" }), ESH.el("div", { class: "v", text: app.reference_number })]),
      ESH.el("div", { class: "detail-item" }, [ESH.el("div", { class: "k", text: "Est. processing" }), ESH.el("div", { class: "v", text: app.estimated_processing || "—" })]),
    ]));

    approveBtn.addEventListener("click", () => confirmAndPatch(app, "Approved", "Application approved.", true));
    rejectBtn.addEventListener("click", () => openReject(app));
    updateBtn.addEventListener("click", () => openUpdate(app));
    reqBtn.addEventListener("click", () => openRequestRequirements(app));
    forwardBtn.addEventListener("click", () => openForward(app));

    return card;
  }

  async function patch(app, status, remarks) {
    return ESH.api.patch(`/staff/applications/${app.id}/status`, { status, remarks });
  }

  async function confirmAndPatch(app, status, remarks, ask) {
    if (app.status === status) {
      ESH.showToast(`No change made. This application is already ${status}.`, "error");
      return;
    }
    if (ask) {
      const ok = await ESH.confirmModal(
        `Mark ${app.reference_number} as "${status}"? This updates the timeline shown to the applicant.`,
        { title: `${status} application?`, confirmLabel: `Confirm ${status}`, danger: status === "Rejected" }
      );
      if (!ok) return;
    }
    try {
      await patch(app, status, remarks || "");
      ESH.showToast(`Application marked as ${status}.`, "success");
      setTimeout(() => window.location.reload(), 800);
    } catch (err) {
      ESH.showToast(err.message, "error");
    }
  }

  function openUpdate(app) {
    const options = STATUSES.map(
      (s) => `<option value="${s}"${s === app.status ? " selected" : ""}>${s}</option>`
    ).join("");
    const m = ESH.openModal(
      `
      <p class="modal-sub">Update the current status of ${ESH.esc(app.reference_number)}.</p>
      <div class="form-group"><label for="modalStatus">New Status</label>
        <select id="modalStatus">${options}</select></div>
      <div class="form-group"><label for="modalRemarks">Remarks</label>
        <textarea id="modalRemarks" rows="3" placeholder="Optional note for the applicant..."></textarea></div>
      <div class="modal-actions">
        <button class="btn btn-secondary" data-close-modal>Cancel</button>
        <button class="btn btn-primary" id="modalSaveBtn">Update Status</button>
      </div>`,
      { title: "Update Status" }
    );
    m.el("#modalSaveBtn").addEventListener("click", async () => {
      const btn = m.el("#modalSaveBtn");
      if (m.el("#modalStatus").value === app.status) {
        ESH.showToast(`No change made. This application is already ${app.status}.`, "error");
        return;
      }
      ESH.setLoading(btn, "Saving...");
      try {
        await patch(app, m.el("#modalStatus").value, m.el("#modalRemarks").value.trim());
        ESH.showToast(`Status updated to ${m.el("#modalStatus").value}.`, "success");
        m.close();
        setTimeout(() => window.location.reload(), 700);
      } catch (err) {
        ESH.unsetLoading(btn);
        ESH.showToast(err.message, "error");
      }
    });
  }

  async function openRequestRequirements(app) {
    let requirements = [];
    try {
      const svc = await ESH.api.get(`/services/${app.service_id}`);
      requirements = Array.isArray(svc.requirements) ? svc.requirements : [];
    } catch {
      requirements = [];
    }
    const checkboxes = requirements.length
      ? requirements.map(
          (r) =>
            `<label style="display:flex;align-items:flex-start;gap:10px;font-weight:500;font-size:0.92rem;padding:10px 12px;border:1px solid var(--color-border);border-radius:10px;background:var(--color-surface-soft)"><input type="checkbox" class="rr-item" style="width:18px;height:18px;margin-top:2px" /> <span>${ESH.esc(r)}</span></label>`
        ).join("")
      : `<p class="note">No specific requirements on file for this service.</p>`;

    const m = ESH.openModal(
      `
      <p class="modal-sub">Flag this application so the applicant supplies missing documents.</p>
      <div class="check-list">${checkboxes}</div>
      <div class="form-group" id="rrGroup" style="margin-top:14px"><label for="rrRemarks">Message to the applicant (optional)</label></div>
      <div class="modal-actions">
        <button class="btn btn-secondary" data-close-modal>Cancel</button>
        <button class="btn btn-primary" id="rrSaveBtn">Send Request</button>
      </div>`,
      { title: "Request Requirements" }
    );
    const remarksBox = ESH.el("textarea", { id: "rrRemarks", rows: "3", style: "width:100%", placeholder: "e.g. Please submit the missing documents." });
    m.modal.querySelector("#rrGroup").appendChild(remarksBox);

    m.el("#rrSaveBtn").addEventListener("click", async () => {
      const selected = Array.from(m.modal.querySelectorAll(".rr-item:checked")).map((c) => c.closest("label").innerText.trim());
      const btn = m.el("#rrSaveBtn");
      ESH.setLoading(btn, "Sending...");
      const remarksText = ["Additional requirements requested:"].concat(selected).concat(remarksBox.value.trim() ? [remarksBox.value.trim()] : []).join("\n• ");
      try {
        await patch(app, "Additional Requirements", remarksText);
        ESH.showToast("Requirements request sent.", "success");
        m.close();
        setTimeout(() => window.location.reload(), 700);
      } catch (err) {
        ESH.unsetLoading(btn);
        ESH.showToast(err.message, "error");
      }
    });
  }

  async function openForward(app) {
    let depts = [];
    try {
      const services = await ESH.api.get("/services");
      const map = new Map();
      services.forEach((s) => {
        if (!map.has(s.department_id)) map.set(s.department_id, { id: s.department_id, name: s.department });
      });
      depts = [...map.values()];
    } catch {
      depts = [];
    }

    const m = ESH.openModal(
      `
      <p class="modal-sub">Forward this application to another department. Its status will become "Forwarded".</p>
      <div class="form-group" id="forwardDeptGroup"><label for="forwardDept">Destination Department</label></div>
      <div class="form-group" id="forwardRemarksGroup"><label for="forwardRemarks">Remarks (optional)</label></div>
      <div class="modal-actions">
        <button class="btn btn-secondary" data-close-modal>Cancel</button>
        <button class="btn btn-primary" id="forwardSaveBtn">Forward</button>
      </div>`,
      { title: "Forward to Department" }
    );
    const deptSelect = ESH.el("select", { id: "forwardDept" });
    depts.forEach((d) => {
      deptSelect.appendChild(ESH.el("option", { value: d.id, text: d.name, selected: String(d.id) === String(app.department_id) ? "selected" : undefined }));
    });
    const remarksBox = ESH.el("textarea", { id: "forwardRemarks", rows: "3", style: "width:100%", placeholder: "Optional note..." });
    m.modal.querySelector("#forwardDeptGroup").appendChild(deptSelect);
    m.modal.querySelector("#forwardRemarksGroup").appendChild(remarksBox);

    m.el("#forwardSaveBtn").addEventListener("click", async () => {
      if (!deptSelect.value) {
        ESH.showToast("Select a department to forward to.", "error");
        return;
      }
      const btn = m.el("#forwardSaveBtn");
      ESH.setLoading(btn, "Forwarding...");
      try {
        await ESH.api.post(`/staff/applications/${app.id}/forward`, { department_id: deptSelect.value, remarks: remarksBox.value.trim() });
        ESH.showToast("Application forwarded.", "success");
        m.close();
        setTimeout(() => window.location.reload(), 700);
      } catch (err) {
        ESH.unsetLoading(btn);
        ESH.showToast(err.message, "error");
      }
    });
  }

  function openReject(app) {
    const m = ESH.openModal(
      `
      <p class="modal-sub">Rejecting moves the application to "Rejected". Please state the reason.</p>
      <div class="form-group" id="rejectReasonGroup"><label for="rejectReason">Reason for rejection</label></div>
      <div class="modal-actions">
        <button class="btn btn-secondary" data-close-modal>Cancel</button>
        <button class="btn btn-danger" id="rejectSaveBtn">Reject Application</button>
      </div>`,
      { title: "Reject Application" }
    );
    const reason = ESH.el("textarea", { id: "rejectReason", rows: "3", style: "width:100%", required: "required", placeholder: "e.g. Incomplete documents" });
    m.modal.querySelector("#rejectReasonGroup").appendChild(reason);

    m.el("#rejectSaveBtn").addEventListener("click", async () => {
      if (!reason.value.trim()) {
        ESH.showToast("Please provide a reason.", "error");
        return;
      }
      const btn = m.el("#rejectSaveBtn");
      ESH.setLoading(btn, "Rejecting...");
      try {
        await patch(app, "Rejected", reason.value.trim());
        ESH.showToast("Application rejected.", "success");
        m.close();
        setTimeout(() => window.location.reload(), 700);
      } catch (err) {
        ESH.unsetLoading(btn);
        ESH.showToast(err.message, "error");
      }
    });
  }

  document.addEventListener("DOMContentLoaded", () => {
    if (document.body.dataset.page === "staff-applications") initList();
    if (document.body.dataset.page === "staff-application-details") initDetail();
  });
})();
