/* eSHCAT — staff/applications.js
   Staff application list (filters) + application detail (status updates,
   forwarding, timeline). */

(function () {
  const ESH = window.ESH;

  const STATUSES = [
    "Submitted", "Received", "Under Review", "Additional Requirements",
    "For Verification", "Forwarded", "Approved", "Rejected",
    "Ready for Release", "Completed", "Cancelled",
  ];

  async function initList() {
    const container = document.getElementById("applicationsTable");
    if (!container) return;
    ESH.showLoading(container, "Loading applications...");

    const applyFilters = async () => {
      const params = new URLSearchParams();
      const status = document.getElementById("filterStatus")?.value;
      if (status) params.set("status", status);
      ESH.showLoading(container, "Loading applications...");
      let rows = [];
      try {
        rows = await ESH.api.get(`/staff/applications?${params.toString()}`);
      } catch (err) {
        ESH.showEmpty(container, err.message);
        return;
      }
      renderTable(container, rows);
    };

    document.getElementById("filterStatus")?.addEventListener("change", applyFilters);
    document.getElementById("refreshBtn")?.addEventListener("click", applyFilters);
    applyFilters();
  }

  function renderTable(container, rows) {
    if (!rows.length) {
      ESH.showEmpty(container, "No applications found.", `<a class="btn btn-secondary" href="/pages/staff/dashboard.html">Back to Dashboard</a>`);
      return;
    }
    container.innerHTML = "";
    const wrap = ESH.el("div", { class: "table-wrap", style: "border:none;box-shadow:none" }, [
      ESH.el("table", { class: "data" }, [
        ESH.el("thead", {}, [ESH.el("tr", {}, [
          ESH.el("th", { text: "Reference" }),
          ESH.el("th", { text: "Service" }),
          ESH.el("th", { text: "Applicant" }),
          ESH.el("th", { text: "Department" }),
          ESH.el("th", { text: "Status" }),
          ESH.el("th", { text: "Submitted" }),
          ESH.el("th", {}, [
            ESH.el("span", { class: "sr-only", text: "Action" }),
          ]),
        ])]),
        ESH.el("tbody", {}, rows.map((a) =>
          ESH.el("tr", {}, [
            ESH.el("td", {}, [ESH.el("a", { href: `/pages/staff/application-details.html?id=${a.id}`, text: a.reference_number })]),
            ESH.el("td", { text: a.service_name }),
            ESH.el("td", { text: a.full_name }),
            ESH.el("td", { text: a.department_name }),
            ESH.el("td", {}, [ESH.statusBadge(a.status)]),
            ESH.el("td", { text: ESH.fmtDate(a.created_at) }),
            ESH.el("td", {}, [ESH.el("a", { class: "btn btn-sm btn-secondary", href: `/pages/staff/application-details.html?id=${a.id}`, text: "View" })]),
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
      ESH.showEmpty(container, err.message);
      return;
    }

    container.innerHTML = "";

    const head = ESH.el("div", { style: "margin-bottom:20px" }, [
      ESH.el("a", { class: "note", href: "/pages/staff/applications.html", text: "← Applications" }),
      ESH.el("div", { class: "dept", style: "margin-top:8px", text: app.department_name }),
      ESH.el("h1", { style: "margin:2px 0 6px", text: app.reference_number }),
      ESH.el("p", { class: "note", text: `${app.service_name} · submitted ${ESH.fmtDateTime(app.created_at)}` }),
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
      ESH.el("h3", { text: "👤 Applicant" }),
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
      ESH.el("h3", { text: "📝 Request Information" }),
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
      ESH.el("h3", { text: "🕓 Application History" }),
      ul,
    ]);
  }

  function actionsCard(app) {
    const card = ESH.el("div", { class: "card sticky-form" }, [
      ESH.el("h3", { text: "Application Status" }),
      ESH.el("div", { style: "margin:12px 0" }, [ESH.statusBadge(app.status)]),
      ESH.el("p", { class: "note", text: "Saved: " + ESH.fmtDateTime(app.updated_at) }),

      ESH.el("label", { for: "newStatus", text: "New Status" }),
      (() => {
        const select = ESH.el("select", { id: "newStatus" });
        STATUSES.forEach((s) => select.appendChild(ESH.el("option", { value: s, text: s, selected: s === app.status ? "selected" : undefined })));
        return select;
      })(),

      ESH.el("label", { for: "remarksInput", text: "Remarks" }),
      ESH.el("textarea", { id: "remarksInput", name: "remarks", rows: "3", placeholder: "Optional note for the applicant..." }),

      ESH.el("div", { class: "status-actions" }, [
        ESH.el("button", { class: "btn btn-primary", id: "saveStatusBtn", text: "Update Status" }),
      ]),

      ESH.el("hr", { style: "border:none;border-top:1px solid var(--color-border);margin:18px 0" }),
      ESH.el("h3", { text: "Forward to Department" }),
      ESH.el("select", { id: "forwardDept" }),
      ESH.el("button", { class: "btn btn-secondary", id: "forwardBtn", text: "Forward" }),
    ]);

    card.querySelector("#saveStatusBtn").addEventListener("click", async () => {
      const status = card.querySelector("#newStatus").value;
      const remarks = card.querySelector("#remarksInput").value;
      const btn = card.querySelector("#saveStatusBtn");
      ESH.setLoading(btn, "Saving...");
      try {
        await ESH.api.patch(`/staff/applications/${app.id}/status`, { status, remarks });
        ESH.showToast(`Status updated to ${status}.`, "success");
        setTimeout(() => window.location.reload(), 700);
      } catch (err) {
        ESH.unsetLoading(btn);
        ESH.showToast(err.message, "error");
      }
    });

    (async () => {
      try {
        const services = await ESH.api.get("/services");
        const depts = {};
        services.forEach((s) => (depts[s.department_id] = s.department));
        const select = card.querySelector("#forwardDept");
        Object.entries(depts).forEach(([id, name]) => {
          select.appendChild(ESH.el("option", { value: id, text: name, selected: String(id) === String(app.department_id) ? "selected" : undefined }));
        });
      } catch {
        /* ignore */
      }
    })();

    card.querySelector("#forwardBtn").addEventListener("click", async () => {
      const department_id = card.querySelector("#forwardDept").value;
      if (!department_id) {
        ESH.showToast("Select a department to forward to.", "error");
        return;
      }
      const btn = card.querySelector("#forwardBtn");
      ESH.setLoading(btn, "Forwarding...");
      try {
        await ESH.api.post(`/staff/applications/${app.id}/forward`, { department_id, remarks: "" });
        ESH.showToast("Application forwarded.", "success");
        setTimeout(() => window.location.reload(), 700);
      } catch (err) {
        ESH.unsetLoading(btn);
        ESH.showToast(err.message, "error");
      }
    });

    return card;
  }

  document.addEventListener("DOMContentLoaded", () => {
    if (document.body.dataset.page === "staff-applications") initList();
    if (document.body.dataset.page === "staff-application-details") initDetail();
  });
})();