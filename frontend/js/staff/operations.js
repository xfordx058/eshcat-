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
    },
    "staff-reports": {
      endpoint: "/staff/reports",
      title: "Reports",
      subtitle: "Review community concerns submitted by residents.",
      columns: ["reference_number", "category", "location", "description", "name", "status", "created_at"],
      labels: ["Reference", "Category", "Location", "Description", "Reporter", "Status", "Submitted"],
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

  function drawTable(container, rows, cfg) {
    if (!rows.length) {
      ESH.showEmpty(container, `No ${cfg.title.toLowerCase()} found.`);
      return;
    }
    container.innerHTML = "";
    const table = ESH.el("table", { class: "data" }, [
      ESH.el("thead", {}, [ESH.el("tr", {}, cfg.labels.map((label) => ESH.el("th", { text: label })))]),
      ESH.el("tbody", {}, rows.map((row) => ESH.el("tr", {}, cfg.columns.map((key) => {
        const text = String(value(row, key));
        return ESH.el("td", { "data-label": cfg.labels[cfg.columns.indexOf(key)] }, [
          key === "status" ? ESH.statusBadge(text) : ESH.el("span", { text }),
        ]);
      })))),
    ]);
    container.appendChild(ESH.el("div", { class: "table-wrap" }, [table]));
  }

  async function initList(cfg) {
    const container = document.getElementById("operationsTable");
    if (!container) return;
    ESH.showLoading(container, `Loading ${cfg.title.toLowerCase()}...`);
    try {
      const rows = await ESH.api.get(cfg.endpoint);
      drawTable(container, rows, cfg);
    } catch (err) {
      ESH.showError(container, err.message, () => initList(cfg));
    }
    document.getElementById("refreshBtn")?.addEventListener("click", () => initList(cfg));
  }

  async function initSettings() {
    const container = document.getElementById("settingsPanel");
    if (!container) return;
    try {
      const user = await ESH.api.get("/staff/me");
      container.innerHTML = "";
      container.appendChild(ESH.el("div", { class: "card", style: "margin-bottom:20px" }, [
        ESH.el("h2", { text: "Staff Account" }),
        ESH.el("p", { class: "note", style: "margin:8px 0 20px", text: "Your account information from the staff database." }),
        ESH.el("div", { class: "detail-list" }, [
          ESH.el("div", { class: "detail-item" }, [ESH.el("div", { class: "k", text: "Name" }), ESH.el("div", { class: "v", text: user.name })]),
          ESH.el("div", { class: "detail-item" }, [ESH.el("div", { class: "k", text: "Role" }), ESH.el("div", { class: "v", text: user.role })]),
          ESH.el("div", { class: "detail-item" }, [ESH.el("div", { class: "k", text: "Department ID" }), ESH.el("div", { class: "v", text: String(user.department_id ?? "All") })]),
        ]),
      ]));
      await renderEmergencyNumbers(container);
      if (user.role !== "Administrator") {
        container.appendChild(ESH.el("div", { class: "card" }, [
          ESH.el("h3", { text: "Staff Management" }),
          ESH.el("p", { class: "note", text: "Only Administrator accounts can add or edit staff users." }),
        ]));
        return;
      }
      await renderStaffManagement(container);
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
        ESH.el("button", { class: "btn btn-secondary btn-sm", type: "button", id: "addNumBtn", text: "+ Add Number" }),
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
      rows.forEach((row, index) => {
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
        ESH.el("button", { class: "btn btn-primary", type: "button", id: "addStaffBtn", text: "+ Add Staff" }),
      ]),
      ESH.el("div", { id: "staffUsersTable" }),
    ]);
    container.appendChild(management);
    drawStaffUsers(users);
    management.querySelector("#addStaffBtn").addEventListener("click", () => openStaffForm(null, departmentOptions));

    function drawStaffUsers(rows) {
      const table = ESH.el("table", { class: "data" }, [
        ESH.el("thead", {}, [ESH.el("tr", {}, ["Name", "Email", "Role", "Department", "Status", "Action"].map((text) => ESH.el("th", { text })))]),
        ESH.el("tbody", {}, rows.map((staff) => ESH.el("tr", {}, [
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
      target.appendChild(ESH.el("div", { class: "table-wrap" }, [table]));
      target.querySelectorAll(".edit-staff").forEach((button, index) => button.addEventListener("click", () => openStaffForm(rows[index], departmentOptions)));
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
