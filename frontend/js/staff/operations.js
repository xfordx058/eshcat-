/* Staff operational pages: appointments, reports, messages, and settings. */
(function () {
  const ESH = window.ESH;
  const page = document.body.dataset.page;

  const configs = {
    "staff-appointments": {
      endpoint: "/staff/appointments",
      title: "Appointments",
      subtitle: "Review citizen appointment requests.",
      columns: ["reference_number", "full_name", "service_name", "department_name", "appointment_date", "appointment_time", "status"],
      labels: ["Reference", "Applicant", "Service", "Department", "Date", "Time", "Status"],
      statusOptions: ["Pending", "Confirmed", "Completed", "Cancelled"],
      updateEndpoint: (row) => `/civil/admin/appointments/${row.id}`,
    },
    "staff-reports": {
      endpoint: "/staff/reports",
      title: "Reports",
      subtitle: "Review community concerns submitted by residents.",
      columns: ["reference_number", "category", "location", "description", "name", "status", "created_at"],
      labels: ["Reference", "Category", "Location", "Description", "Reporter", "Status", "Submitted"],
      statusOptions: ["Open", "In Progress", "Resolved"],
      updateEndpoint: (row) => `/civil/admin/reports/${row.id}`,
    },
    "staff-messages": {
      endpoint: "/staff/messages",
      title: "Messages",
      subtitle: "Review notification messages generated for applicants.",
      columns: ["recipient_email", "subject", "reference_number", "status", "created_at"],
      labels: ["Recipient", "Subject", "Reference", "Status", "Created"],
    },
  };

  function value(row, key) {
    if (key === "created_at") return ESH.fmtDateTime(row[key]);
    if (key === "appointment_date") return ESH.fmtDate(row[key]);
    return row[key] ?? "—";
  }

  function drawTable(container, rows, cfg, onView) {
    if (!rows.length) {
      ESH.showEmpty(container, `No ${cfg.title.toLowerCase()} found.`);
      return;
    }
    container.innerHTML = "";
    const headerCells = cfg.labels.concat(cfg.updateEndpoint ? ["Action"] : []).map((label) => ESH.el("th", { text: label }));
    const bodyRows = rows.map((row) => {
      const cells = cfg.columns.map((key, index) => {
        const text = String(value(row, key));
        return ESH.el("td", { "data-label": cfg.labels[index] }, [key === "status" ? ESH.statusBadge(text) : ESH.el("span", { text })]);
      });
      if (cfg.updateEndpoint) cells.push(ESH.el("td", { "data-label": "Action" }, [ESH.el("button", { class: "btn btn-secondary btn-sm", type: "button", text: "View" })]));
      return ESH.el("tr", {}, cells);
    });
    const table = ESH.el("table", { class: "data" }, [
      ESH.el("thead", {}, [ESH.el("tr", {}, headerCells)]),
      ESH.el("tbody", {}, bodyRows),
    ]);
    container.appendChild(ESH.el("div", { class: "table-wrap" }, [table]));
    if (cfg.updateEndpoint) table.querySelectorAll("tbody button").forEach((button, index) => button.addEventListener("click", () => onView(rows[index])));
  }

  async function initList(cfg) {
    const container = document.getElementById("operationsTable");
    if (!container) return;
    if (cfg.title === "Reports") renderDailyReportSummary(container);
    if (cfg.statusOptions && !document.getElementById("operationsStatusFilter")) {
      const filter = ESH.el("select", { id: "operationsStatusFilter", "aria-label": `Filter ${cfg.title.toLowerCase()} status`, style: "margin-bottom:12px" }, [
        ESH.el("option", { value: "", text: `All ${cfg.title}` }),
        ...cfg.statusOptions.map((status) => ESH.el("option", { value: status, text: status })),
      ]);
      container.parentElement.insertBefore(filter, container);
    }
    const load = async () => {
      ESH.showLoading(container, `Loading ${cfg.title.toLowerCase()}...`);
      try {
        const rows = await ESH.api.get(cfg.endpoint);
        const filter = document.getElementById("operationsStatusFilter")?.value || "";
        const filtered = filter ? rows.filter((row) => row.status === filter) : rows;
        drawTable(container, filtered, cfg, (row) => openRecordDetail(row, cfg, load));
      } catch (err) { ESH.showError(container, err.message, load); }
    };
    document.getElementById("refreshBtn")?.addEventListener("click", load);
    document.getElementById("operationsStatusFilter")?.addEventListener("change", load);
    load();
  }

  async function renderDailyReportSummary(tableContainer) {
    let summary = document.getElementById("dailyReportSummary");
    if (!summary) {
      summary = ESH.el("section", { id: "dailyReportSummary", class: "daily-report-summary card" });
      tableContainer.parentElement.insertBefore(summary, tableContainer);
    }
    ESH.showLoading(summary, "Loading today’s report...");
    try {
      const data = await ESH.api.get("/staff/reports/daily-summary");
      const totals = data.totals || {};
      const total = Number(totals.total || 0);
      const resolved = Number(totals.Resolved || 0);
      const percent = total ? Math.round((resolved / total) * 100) : 0;
      summary.innerHTML = "";
      summary.appendChild(ESH.el("div", { class: "daily-report-heading" }, [
        ESH.el("div", {}, [ESH.el("h2", { text: "Today’s Reports" }), ESH.el("p", { class: "note", text: `${data.date} · Community concerns received today` })]),
        ESH.el("a", { class: "btn btn-secondary btn-sm", href: "/api/staff/reports/daily.pdf", download: "eshcat-daily-reports.pdf" }, [ESH.el("i", { class: "fa-solid fa-file-pdf", "aria-hidden": "true" }), " Download PDF"]),
      ]));
      const ring = ESH.el("div", { class: "report-ring", style: `--ring-progress:${percent * 3.6}deg` }, [
        ESH.el("div", { class: "report-ring__center" }, [ESH.el("strong", { text: String(total) }), ESH.el("span", { text: "Total" })]),
      ]);
      summary.appendChild(ESH.el("div", { class: "daily-report-content" }, [
        ring,
        ESH.el("div", { class: "daily-report-stats" }, [
          reportStat("Open", totals.Open || 0, "open"), reportStat("In Progress", totals["In Progress"] || 0, "progress"), reportStat("Resolved", totals.Resolved || 0, "resolved"),
        ]),
      ]));
    } catch (err) { ESH.showError(summary, err.message, () => renderDailyReportSummary(tableContainer)); }
  }

  function reportStat(label, value, tone) {
    return ESH.el("div", { class: "report-stat" }, [ESH.el("span", { class: `report-stat__dot ${tone}` }), ESH.el("span", { text: label }), ESH.el("strong", { text: String(value) })]);
  }

  function openRecordDetail(row, cfg, reload) {
    const isReport = cfg.title === "Reports";
    const image = isReport && /^data:image\//.test(row.photo_data || "")
      ? `<img src="${ESH.esc(row.photo_data)}" alt="Attached report evidence" style="display:block;width:100%;max-height:360px;object-fit:contain;border-radius:12px;background:#f1f5f9;margin-top:10px">`
      : "";
    const modal = ESH.openModal(`
      <div class="record-detail-grid">
        <div><span class="detail-label">Reference</span><strong>${ESH.esc(row.reference_number || "—")}</strong></div>
        <div><span class="detail-label">${isReport ? "Reporter" : "Applicant"}</span><strong>${ESH.esc(row.full_name || row.name || "—")}</strong></div>
        <div><span class="detail-label">Email</span><strong>${ESH.esc(row.email || "—")}</strong></div>
        <div><span class="detail-label">${isReport ? "Category" : "Service"}</span><strong>${ESH.esc(isReport ? row.category : row.service_name || "—")}</strong></div>
        <div><span class="detail-label">${isReport ? "Location" : "Office"}</span><strong>${ESH.esc(isReport ? row.location || "—" : row.department_name || "—")}</strong></div>
        ${!isReport ? `<div><span class="detail-label">Schedule</span><strong>${ESH.esc(`${row.appointment_date || "—"} · ${row.appointment_time || "—"}`)}</strong></div>` : ""}
      </div>
      <div class="record-description"><span class="detail-label">${isReport ? "Concern details" : "Remarks"}</span><p>${ESH.esc(isReport ? row.description || "—" : row.remarks || "No remarks yet.")}</p></div>
      ${image ? `<div class="record-attachment"><span class="detail-label">Attached photo</span>${image}</div>` : ""}
      <div class="form-group" style="margin-top:18px"><label for="recordStatus">Update status</label><select id="recordStatus">${cfg.statusOptions.map((status) => `<option value="${ESH.esc(status)}"${status === row.status ? " selected" : ""}>${ESH.esc(status)}</option>`).join("")}</select></div>
      <div class="form-group"><label for="recordRemarks">Staff remarks</label><textarea id="recordRemarks" rows="3" placeholder="Add a note that will be sent to the requester...">${ESH.esc(row.remarks || "")}</textarea></div>
      <div class="modal-actions"><button class="btn btn-secondary" data-close-modal type="button">Close</button><button class="btn btn-primary" id="saveRecordBtn" type="button">Save Status Update</button></div>`,
      { title: isReport ? "Report Details" : "Appointment Details" }
    );
    modal.el("#saveRecordBtn").addEventListener("click", async () => {
      const status = modal.el("#recordStatus").value;
      const remarks = modal.el("#recordRemarks").value.trim();
      if (status === row.status && remarks === (row.remarks || "")) {
        ESH.showToast("No changes to save.", "error");
        return;
      }
      const button = modal.el("#saveRecordBtn");
      ESH.setLoading(button, "Saving...");
      try {
        await ESH.api.patch(cfg.updateEndpoint(row), { status, remarks });
        modal.close();
        ESH.showToast("Status updated successfully.", "success");
        reload();
      } catch (err) { ESH.unsetLoading(button); ESH.showToast(err.message, "error"); }
    });
  }

  async function loadOfficeManagement(panel) {
    panel.innerHTML = "";
    const header = ESH.el("div", { class: "section-header", style: "display:flex;justify-content:space-between;align-items:flex-end;gap:16px;flex-wrap:wrap" }, [
      ESH.el("div", {}, [
        ESH.el("h2", { text: "Office Management" }),
        ESH.el("p", { text: "Edit office contact information shown to residents. Add or delete offices when needed." }),
      ]),
      ESH.el("button", { class: "btn btn-primary btn-sm", id: "addOfficeBtn", type: "button" }, [ESH.el("i", { class: "fa-solid fa-plus", "aria-hidden": "true" }), " Add Office"]),
    ]);
    const container = ESH.el("div", { class: "bento-grid", id: "officeManagement" });
    panel.appendChild(header);
    panel.appendChild(container);
    ESH.showLoading(container, "Loading office information...");
    try {
      const offices = await ESH.api.get("/staff/departments");
      container.innerHTML = "";
      if (!offices.length) {
        ESH.showEmpty(container, "No offices have been added yet.");
      } else {
        offices.forEach((office) => {
          const serviceRows = (office.services || []).map((service) => `
            <div class="office-service-row"><span>${ESH.esc(service.name)}</span>
              <input class="office-processing" data-service-id="${service.id}" value="${ESH.esc(service.estimated_processing || "")}" placeholder="e.g. 5-7 working days" />
              <button class="btn btn-ghost btn-sm save-processing" data-service-id="${service.id}" type="button">Save</button>
            </div>`).join("");
          const card = ESH.el("div", { class: "card bento-6 office-management-card" }, [
            ESH.el("div", { style: "display:flex;justify-content:space-between;align-items:flex-start;gap:12px" }, [
              ESH.el("div", {}, [ESH.el("h3", { text: office.name }), ESH.el("p", { class: "note", text: office.description || "No office description." })]),
              ESH.el("div", { style: "display:flex;gap:6px;flex-shrink:0" }, [
                ESH.el("button", { class: "btn btn-secondary btn-sm edit-office", "data-office-id": office.id, type: "button", text: "Edit" }),
                ESH.el("button", { class: "btn btn-danger btn-sm delete-office", "data-office-id": office.id, type: "button", text: "Delete" }),
              ]),
            ]),
            ESH.el("div", { class: "detail-list", style: "margin-top:14px" }, [
              detailItem("Office location", office.location), detailItem("Contact number", office.contact_number),
              detailItem("Office hours", office.office_hours), detailItem("Email", office.email),
            ]),
            serviceRows ? ESH.el("div", { class: "office-services" }, [ESH.el("h4", { text: "Service processing times" }), ESH.el("div", { class: "office-service-list" })]) : "",
          ]);
          if (serviceRows) card.querySelector(".office-service-list").innerHTML = serviceRows;
          container.appendChild(card);
        });
      }
      container.querySelectorAll(".edit-office").forEach((button) => button.addEventListener("click", () => openOfficeForm(offices.find((office) => String(office.id) === button.dataset.officeId), panel)));
      container.querySelectorAll(".delete-office").forEach((button) => button.addEventListener("click", async () => {
        const office = offices.find((item) => String(item.id) === button.dataset.officeId);
        if (!(await ESH.confirmModal(`Delete ${office.name}? Offices with linked services or records cannot be deleted.`, { title: "Delete office?", confirmLabel: "Delete", danger: true }))) return;
        try { await ESH.api.delete(`/staff/departments/${office.id}`); ESH.showToast("Office deleted.", "success"); loadOfficeManagement(panel); }
        catch (err) { ESH.showToast(err.message, "error"); }
      }));
      container.querySelectorAll(".save-processing").forEach((button) => button.addEventListener("click", async () => {
        const input = container.querySelector(`.office-processing[data-service-id="${button.dataset.serviceId}"]`);
        try { await ESH.api.patch(`/staff/services/${button.dataset.serviceId}`, { estimated_processing: input.value.trim() }); ESH.showToast("Processing time updated.", "success"); }
        catch (err) { ESH.showToast(err.message, "error"); }
      }));
      panel.querySelector("#addOfficeBtn").addEventListener("click", () => openOfficeForm(null, panel));
    } catch (err) { ESH.showError(container, err.message, () => loadOfficeManagement(panel)); }
  }

  function detailItem(label, value) {
    return ESH.el("div", { class: "detail-item" }, [ESH.el("div", { class: "k", text: label }), ESH.el("div", { class: "v", text: value || "—" })]);
  }

  function openOfficeForm(office, panel) {
    const editing = Boolean(office);
    const modal = ESH.openModal(`
      <div class="form-group"><label for="officeName">Office name</label><input id="officeName" value="${ESH.esc(office?.name || "")}" required></div>
      <div class="form-group"><label for="officeDescription">Description</label><textarea id="officeDescription" rows="2">${ESH.esc(office?.description || "")}</textarea></div>
      <div class="form-group"><label for="officeLocation">Office location</label><input id="officeLocation" value="${ESH.esc(office?.location || "")}" placeholder="Municipal Hall, Catarman, Northern Samar"></div>
      <div class="form-group"><label for="officeContact">Contact number</label><input id="officeContact" value="${ESH.esc(office?.contact_number || "")}"></div>
      <div class="form-group"><label for="officeHours">Office hours</label><input id="officeHours" value="${ESH.esc(office?.office_hours || "")}" placeholder="Mon-Fri 8:00 AM - 5:00 PM"></div>
      <div class="form-group"><label for="officeEmail">Email</label><input id="officeEmail" type="email" value="${ESH.esc(office?.email || "")}"></div>
      <div class="modal-actions"><button class="btn btn-secondary" data-close-modal type="button">Cancel</button><button class="btn btn-primary" id="saveOfficeBtn" type="button">${editing ? "Save Changes" : "Add Office"}</button></div>`,
      { title: editing ? "Edit Office Information" : "Add Office" });
    modal.el("#saveOfficeBtn").addEventListener("click", async () => {
      const name = modal.el("#officeName").value.trim();
      if (!name) { ESH.showToast("Office name is required.", "error"); return; }
      const payload = { name, description: modal.el("#officeDescription").value.trim(), location: modal.el("#officeLocation").value.trim(), contact_number: modal.el("#officeContact").value.trim(), office_hours: modal.el("#officeHours").value.trim(), email: modal.el("#officeEmail").value.trim() };
      const button = modal.el("#saveOfficeBtn"); ESH.setLoading(button, editing ? "Saving..." : "Adding...");
      try { if (editing) await ESH.api.patch(`/staff/departments/${office.id}`, payload); else await ESH.api.post("/staff/departments", payload); modal.close(); ESH.showToast(editing ? "Office information updated." : "Office added.", "success"); loadOfficeManagement(panel); }
      catch (err) { ESH.unsetLoading(button); ESH.showToast(err.message, "error"); }
    });
  }

  async function initSettings() {
    const container = document.getElementById("settingsPanel");
    if (!container) return;
    try {
      const user = await ESH.api.get("/staff/me");
      container.innerHTML = "";
      const accountPanel = ESH.el("div", { "data-settings-panel": "account" });
      accountPanel.appendChild(ESH.el("div", { class: "card", style: "margin-bottom:20px" }, [
        ESH.el("h2", { text: "Staff Account" }),
        ESH.el("p", { class: "note", style: "margin:8px 0 20px", text: "Your account information from the staff database." }),
        ESH.el("div", { class: "detail-list" }, [
          ESH.el("div", { class: "detail-item" }, [ESH.el("div", { class: "k", text: "Name" }), ESH.el("div", { class: "v", text: user.name })]),
          ESH.el("div", { class: "detail-item" }, [ESH.el("div", { class: "k", text: "Role" }), ESH.el("div", { class: "v", text: user.role })]),
          ESH.el("div", { class: "detail-item" }, [ESH.el("div", { class: "k", text: "Department ID" }), ESH.el("div", { class: "v", text: String(user.department_id ?? "All") })]),
        ]),
      ]));

      const emergencyPanel = ESH.el("div", { "data-settings-panel": "emergency", hidden: "hidden" });
      const staffPanel = ESH.el("div", { "data-settings-panel": "staff", hidden: "hidden" });
      const officePanel = ESH.el("div", { "data-settings-panel": "office", hidden: "hidden" });
      const tabs = ESH.el("div", { class: "tabs settings-tabs", "aria-label": "Settings sections" }, [
        ESH.el("button", { class: "tab-btn active", type: "button", "data-settings-tab": "account", text: "Staff Account" }),
        ESH.el("button", { class: "tab-btn", type: "button", "data-settings-tab": "emergency", text: "Emergency & Important Numbers" }),
        ESH.el("button", { class: "tab-btn", type: "button", "data-settings-tab": "staff", text: "Staff Management" }),
        ESH.el("button", { class: "tab-btn", type: "button", "data-settings-tab": "office", text: "Office Management" }),
      ]);
      container.appendChild(tabs);
      container.appendChild(accountPanel);
      container.appendChild(emergencyPanel);
      container.appendChild(staffPanel);
      container.appendChild(officePanel);

      tabs.querySelectorAll("[data-settings-tab]").forEach((button) => {
        button.addEventListener("click", () => {
          tabs.querySelectorAll("[data-settings-tab]").forEach((tab) => tab.classList.toggle("active", tab === button));
          container.querySelectorAll("[data-settings-panel]").forEach((panel) => {
            panel.hidden = panel.dataset.settingsPanel !== button.dataset.settingsTab;
          });
        });
      });

      await renderEmergencyNumbers(emergencyPanel);
      if (user.role !== "Administrator") {
        tabs.querySelector('[data-settings-tab="staff"]').disabled = true;
        tabs.querySelector('[data-settings-tab="office"]').disabled = true;
        staffPanel.innerHTML = "";
        staffPanel.appendChild(ESH.el("div", { class: "card" }, [
          ESH.el("h3", { text: "Staff Management" }),
          ESH.el("p", { class: "note", text: "Only Administrator accounts can add or edit staff users." }),
        ]));
        return;
      }
      await renderStaffManagement(staffPanel);
      await loadOfficeManagement(officePanel);
    } catch (err) {
      ESH.showError(container, err.message, initSettings);
    }
  }

  async function renderEmergencyNumbers(container) {
    let rows = [];
    try {
      rows = await ESH.api.get("/emergency-numbers");
    } catch {
      return;
    }
    const card = ESH.el("div", { class: "card", style: "margin-bottom:20px" }, [
      ESH.el("div", { style: "display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:14px" }, [
        ESH.el("div", {}, [
          ESH.el("h2", { text: "Emergency & Important Numbers" }),
          ESH.el("p", { class: "note", text: "Edit the numbers shown on the public homepage. Saving overwrites the list." }),
        ]),
        ESH.el("div", { style: "display:flex;gap:8px;align-items:center;flex-wrap:wrap" }, [
          ESH.el("input", { id: "numSearch", type: "search", placeholder: "Search numbers...", "aria-label": "Search emergency numbers" }),
          ESH.el("button", { class: "btn btn-secondary btn-sm", type: "button", id: "addNumBtn", text: "+ Add Number" }),
        ]),
      ]),
      ESH.el("div", { id: "numEditorRows" }),
      ESH.el("div", { style: "display:flex;justify-content:flex-end;margin-top:14px" }, [
        ESH.el("button", { class: "btn btn-primary", type: "button", id: "saveNumsBtn", text: "Save Numbers" }),
      ]),
    ]);
    container.appendChild(card);

    function render() {
      const wrap = card.querySelector("#numEditorRows");
      wrap.innerHTML = "";
      rows.map((row, index) => ({ row, index })).filter(({ row }) => {
        const query = card.querySelector("#numSearch").value.trim().toLowerCase();
        return !query || `${row.label} ${row.value}`.toLowerCase().includes(query);
      }).forEach(({ row, index }) => {
        const item = ESH.el("div", { class: "emergency-number-row", style: "display:grid;grid-template-columns:1fr 1.2fr auto;gap:12px;align-items:end;margin-bottom:12px" }, [
          ESH.el("div", { class: "form-group" }, [
            ESH.el("label", { text: "Label" }),
            ESH.el("input", { id: `numLabel_${index}`, value: row.label }),
          ]),
          ESH.el("div", { class: "form-group" }, [
            ESH.el("label", { text: "Number" }),
            ESH.el("input", { id: `numValue_${index}`, value: row.value }),
          ]),
          ESH.el("button", { class: "btn btn-sm btn-secondary", type: "button", "data-rm": String(index), text: "Remove" }),
        ]);
        wrap.appendChild(item);
      });
      wrap.querySelectorAll("[data-rm]").forEach((btn) =>
        btn.addEventListener("click", () => {
          rows.splice(Number(btn.dataset.rm), 1);
          render();
        })
      );
    }
    render();

    card.querySelector("#addNumBtn").addEventListener("click", () => {
      rows.push({ label: "", value: "" });
      render();
    });
    card.querySelector("#numSearch").addEventListener("input", render);

    card.querySelector("#saveNumsBtn").addEventListener("click", async () => {
      const cleaned = rows.map((row, index) => ({
        label: card.querySelector(`#numLabel_${index}`).value.trim(),
        value: card.querySelector(`#numValue_${index}`).value.trim(),
      }));
      if (!cleaned.length) { ESH.showToast("Save at least one number.", "error"); return; }
      if (cleaned.some((x) => !x.label || !x.value)) { ESH.showToast("Every number needs both a label and a value.", "error"); return; }
      const btn = card.querySelector("#saveNumsBtn");
      ESH.setLoading(btn, "Saving...");
      try {
        const saved = await ESH.api.put("/staff/emergency-numbers", { items: cleaned });
        rows = saved.items;
        render();
        ESH.showToast("Emergency numbers updated.", "success");
      } catch (err) {
        ESH.showToast(err.message, "error");
      }
      ESH.unsetLoading(btn, "Save Numbers");
    });
  }

  async function renderStaffManagement(container) {
    const [users, departments] = await Promise.all([
      ESH.api.get("/staff/users"),
      ESH.api.get("/staff/departments"),
    ]);
    const departmentOptions = departments.map((d) => ({ value: String(d.id), text: d.name }));
    const management = ESH.el("div", { class: "card" }, [
      ESH.el("div", { style: "display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:14px" }, [
        ESH.el("div", {}, [ESH.el("h2", { text: "Staff Management" }), ESH.el("p", { class: "note", text: "Add staff accounts, assign roles, and edit department access." })]),
        ESH.el("div", { style: "display:flex;gap:8px;align-items:center;flex-wrap:wrap" }, [
          ESH.el("input", { id: "staffSearch", type: "search", placeholder: "Search staff...", "aria-label": "Search staff" }),
          ESH.el("button", { class: "btn btn-primary", type: "button", id: "addStaffBtn", text: "+ Add Staff" }),
        ]),
      ]),
      ESH.el("div", { id: "staffUsersTable" }),
    ]);
    container.appendChild(management);
    drawStaffUsers(users);
    management.querySelector("#addStaffBtn").addEventListener("click", () => openStaffForm(null, departmentOptions));
    management.querySelector("#staffSearch").addEventListener("input", () => drawStaffUsers(users));

    function drawStaffUsers(rows) {
      const query = management.querySelector("#staffSearch").value.trim().toLowerCase();
      const filtered = rows.filter((staff) => !query || [staff.name, staff.email, staff.role, staff.department_name].some((value) => String(value || "").toLowerCase().includes(query)));
      const table = ESH.el("table", { class: "data" }, [
        ESH.el("thead", {}, [ESH.el("tr", {}, ["Name", "Email", "Role", "Department", "Status", "Action"].map((text) => ESH.el("th", { text })))]),
        ESH.el("tbody", {}, filtered.map((staff) => ESH.el("tr", {}, [
          ESH.el("td", { "data-label": "Name", text: staff.name }),
          ESH.el("td", { "data-label": "Email", text: staff.email }),
          ESH.el("td", { "data-label": "Role", text: staff.role }),
          ESH.el("td", { "data-label": "Department", text: staff.department_name || "All departments" }),
          ESH.el("td", { "data-label": "Status" }, [ESH.statusBadge(staff.is_active ? "Active" : "Inactive")]),
          ESH.el("td", { "data-label": "Action" }, [ESH.el("button", { class: "btn btn-sm btn-secondary edit-staff", type: "button", text: "Edit" })]),
        ]))),
      ]);
      const target = management.querySelector("#staffUsersTable");
      target.innerHTML = "";
      if (!filtered.length) {
        target.appendChild(ESH.el("p", { class: "note", text: "No staff accounts match your search." }));
        return;
      }
      target.appendChild(ESH.el("div", { class: "table-wrap" }, [table]));
      target.querySelectorAll(".edit-staff").forEach((button, index) => button.addEventListener("click", () => openStaffForm(filtered[index], departmentOptions)));
    }

    async function refreshUsers() {
      const latest = await ESH.api.get("/staff/users");
      drawStaffUsers(latest);
    }

    function openStaffForm(staff, options) {
      const editing = Boolean(staff);
      const departmentSelect = options.map((d) => `<option value="${ESH.esc(d.value)}"${staff && String(staff.department_id) === d.value ? " selected" : ""}>${ESH.esc(d.text)}</option>`).join("");
      const modal = ESH.openModal(`
        <div class="form-group"><label for="staffName">Full name</label><input id="staffName" value="${ESH.esc(staff?.name || "")}" required></div>
        <div class="form-group"><label for="staffEmail">Email</label><input id="staffEmail" type="email" value="${ESH.esc(staff?.email || "")}" required></div>
        <div class="form-group"><label for="staffRole">Role</label><select id="staffRole"><option${staff?.role === "Staff" ? " selected" : ""}>Staff</option><option${staff?.role === "Department Head" ? " selected" : ""}>Department Head</option><option${staff?.role === "Administrator" ? " selected" : ""}>Administrator</option><option${staff?.role === "Auditor" ? " selected" : ""}>Auditor</option></select></div>
        <div class="form-group"><label for="staffDepartment">Department</label><select id="staffDepartment"><option value="">All departments</option>${departmentSelect}</select></div>
        <div class="form-group"><label for="staffPassword">${editing ? "New password (optional)" : "Password"}</label><input id="staffPassword" type="password" ${editing ? "" : "required"} placeholder="At least 6 characters"></div>
        ${editing ? `<label style="display:flex;gap:8px;align-items:center"><input id="staffActive" type="checkbox"${staff.is_active ? " checked" : ""}> Active account</label>` : ""}
        <div class="modal-actions"><button class="btn btn-secondary" data-close-modal type="button">Cancel</button><button class="btn btn-primary" id="saveStaffBtn" type="button">${editing ? "Save Changes" : "Create Staff"}</button></div>`,
        { title: editing ? "Edit Staff" : "Add Staff" }
      );
      modal.el("#saveStaffBtn").addEventListener("click", async () => {
        const password = modal.el("#staffPassword").value;
        if (!editing && password.length < 6) { ESH.showToast("Password must be at least 6 characters.", "error"); return; }
        const payload = { name: modal.el("#staffName").value.trim(), email: modal.el("#staffEmail").value.trim(), role: modal.el("#staffRole").value, department_id: modal.el("#staffDepartment").value || null };
        if (password) payload.password = password;
        if (editing) payload.is_active = modal.el("#staffActive").checked;
        const button = modal.el("#saveStaffBtn");
        ESH.setLoading(button, editing ? "Saving..." : "Creating...");
        try {
          if (editing) await ESH.api.patch(`/staff/users/${staff.id}`, payload);
          else await ESH.api.post("/staff/users", payload);
          modal.close();
          ESH.showToast(editing ? "Staff account updated." : "Staff account created.", "success");
          await refreshUsers();
        } catch (err) { ESH.unsetLoading(button); ESH.showToast(err.message, "error"); }
      });
    }
  }

  document.addEventListener("DOMContentLoaded", () => {
    if (configs[page]) initList(configs[page]);
    if (page === "staff-settings") initSettings();
  });
})();
