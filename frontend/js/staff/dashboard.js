/* eSHCAT — staff/dashboard.js
   Staff dashboard: bento stat cards, CSS bar chart, quick summary,
   recent applications table. */

(function () {
  const ESH = window.ESH;

  const TILES = [
    { key: "total", label: "Total Applications", icon: "folder", tone: "info" },
    { key: "Pending", label: "Pending Review", icon: "clock", tone: "warning" },
    { key: "Under Review", label: "Under Review", icon: "refresh", tone: "purple" },
    { key: "Completed", label: "Completed", icon: "check", tone: "success" },
    { key: "appointments", label: "Pending Appointments", icon: "calendar", tone: "info" },
    { key: "reports", label: "Open Reports", icon: "flag", tone: "danger" },
  ];

  const ICONS = {
    folder: '<path d="M3.75 6.75A2.25 2.25 0 0 1 6 4.5h3l1.5 1.75h7.5A2.25 2.25 0 0 1 20.25 8.5v7.25A2.25 2.25 0 0 1 18 18H6a2.25 2.25 0 0 1-2.25-2.25V6.75Z"/><path d="M3.75 8.5h16.5"/>',
    clock: '<circle cx="12" cy="12" r="8.25"/><path d="M12 7.5v4.75l3.25 1.75"/>',
    refresh: '<path d="M19.5 8.25A8.25 8.25 0 0 0 5.6 6.4L4 8"/><path d="M4 4.5v3.5h3.5"/><path d="M4.5 15.75a8.25 8.25 0 0 0 13.9 1.85L20 16"/><path d="M20 19.5V16h-3.5"/>',
    check: '<circle cx="12" cy="12" r="8.25"/><path d="m8.25 12.1 2.5 2.5 5-5"/>',
    calendar: '<rect x="4" y="5.25" width="16" height="14" rx="2"/><path d="M8 3.75v3M16 3.75v3M4 9.25h16"/><path d="M8 13h.01M12 13h.01M16 13h.01M8 16h.01M12 16h.01"/>',
    flag: '<path d="M6 20V4.5"/><path d="M6 5c4-3 7 3 12 0v8c-5 3-8-3-12 0"/>',
  };

  function iconSvg(name) {
    const icon = ESH.el("span", { class: "stat-icon-svg", "aria-hidden": "true" });
    icon.innerHTML = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">${ICONS[name] || ICONS.folder}</svg>`;
    return icon;
  }

  const CHART_SERIES = [
    { key: "Pending", label: "Pending" },
    { key: "Received", label: "Received" },
    { key: "Under Review", label: "Review" },
    { key: "Approved", label: "Approved" },
    { key: "Rejected", label: "Rejected" },
    { key: "Completed", label: "Done" },
  ];

  function buildStats(stats, container) {
    container.innerHTML = "";
    const wrapper = ESH.el("div", { class: "bento-grid" });
    TILES.forEach((t) => {
      const card = ESH.el("div", { class: "stat-card bento-3" }, [
        ESH.el("div", { class: `stat-icon ${t.tone}`, "aria-label": t.label }, [iconSvg(t.icon)]),
        ESH.el("div", {}, [
          ESH.el("div", { class: "num", text: String(stats[t.key] ?? 0) }),
          ESH.el("div", { class: "label", text: t.label }),
        ]),
      ]);
      wrapper.appendChild(card);
    });
    container.appendChild(wrapper);
  }

  function buildChart(stats, container) {
    const values = CHART_SERIES.map((s) => stats[s.key] ?? 0);
    const max = Math.max(1, ...values);
    container.innerHTML = "";
    const bars = ESH.el("div", { class: "chart-bars" });
    CHART_SERIES.forEach((s, i) => {
      const value = stats[s.key] ?? 0;
      const pct = Math.round((value / max) * 100);
      bars.appendChild(
        ESH.el("div", { class: "chart-bar", title: `${s.label}: ${value}` }, [
          ESH.el("div", { class: "val", text: String(value) }),
          ESH.el("div", { class: "bar", style: `height:${pct}%` }),
          ESH.el("div", { class: "lbl", text: s.label }),
        ])
      );
      bars.querySelectorAll(".bar")[i].style.height = `${pct}%`;
    });
    container.appendChild(bars);
  }

  function buildSummary(stats, container) {
    container.innerHTML = "";
    const rows = [
      ["Today's submissions", stats.today ?? 0],
      ["Received", stats.Received ?? 0],
      ["Approved", stats.Approved ?? 0],
      ["Rejected", stats.Rejected ?? 0],
    ];
    rows.forEach(([label, value]) => {
      container.appendChild(
        ESH.el("div", { style: "display:flex;justify-content:space-between;align-items:center;padding:10px 0;border-bottom:1px solid var(--color-border)" }, [
          ESH.el("span", { style: "font-size:0.88rem;color:var(--color-text-secondary)", text: label }),
          ESH.el("span", { style: "font-weight:800;font-size:1.05rem", text: String(value) }),
        ])
      );
    });
  }

  async function loadActivity(page = 1) {
    const log = document.getElementById("activityLog");
    const pagination = document.getElementById("activityPagination");
    if (!log) return;
    try {
      const data = await ESH.api.get(`/staff/activity?page=${page}`);
      log.innerHTML = data.items.length ? data.items.map((item) =>
        `<div class="activity-row"><span class="activity-icon ${(item.action || "").includes("LOG") ? "auth" : "update"}">${(item.action || "").includes("LOGIN") ? "↗" : (item.action || "").includes("LOGOUT") ? "↙" : "✓"}</span><div class="activity-main"><div class="activity-title"><strong>${esc(item.action || "Activity")}</strong><span>${esc(item.entity_type || "record")} #${esc(item.entity_id || "")}</span></div><span class="activity-details">${esc(item.details || "Activity recorded")}</span></div><small><b>${esc(item.staff_name || "System")}</b>${esc(fmtDateTime(item.created_at))}</small></div>`
      ).join("") : '<div class="empty">No activity recorded yet.</div>';
      const pages = data.pages || 1;
      pagination.innerHTML = `<button type="button" data-activity-page="${Math.max(1, data.page - 1)}" ${data.page <= 1 ? "disabled" : ""}>Previous</button>` +
        Array.from({ length: Math.min(10, pages) }, (_, i) => i + 1).map((p) => `<button type="button" class="${p === data.page ? "active" : ""}" data-activity-page="${p}">${p}</button>`).join("") +
        `<button type="button" data-activity-page="${Math.min(pages, data.page + 1)}" ${data.page >= pages ? "disabled" : ""}>Next</button>`;
    } catch (err) { log.innerHTML = `<div class="empty">${esc(err.message)}</div>`; }
  }

  async function init() {
    const statsEl = document.getElementById("statTiles");
    const chartEl = document.getElementById("appChart");
    const summaryEl = document.getElementById("summaryTiles");
    const recentEl = document.getElementById("recentApplications");
    if (statsEl) ESH.showLoading(statsEl, "Loading dashboard...");

    let stats;
    let recent;
    try {
      [stats, recent] = await Promise.all([
        ESH.api.get("/staff/dashboard"),
        ESH.api.get("/staff/applications"),
      ]);
    } catch (err) {
      if (statsEl) ESH.showError(statsEl, err.message, () => init());
      return;
    }

    if (statsEl) buildStats(stats, statsEl);
    if (chartEl) buildChart(stats, chartEl);
    if (summaryEl) buildSummary(stats, summaryEl);

    if (recentEl) {
      if (!recent.length) {
        ESH.showEmpty(recentEl, "No applications yet.");
      } else {
      recentEl.innerHTML = "";
      const rows = recent.slice(0, 8).map((a) =>
        ESH.el("tr", {}, [
          ESH.el("td", { "data-label": "Reference" }, [ESH.el("a", { href: `/pages/staff/application-details.html?id=${a.id}`, text: a.reference_number })]),
          ESH.el("td", { "data-label": "Service", text: a.service_name }),
          ESH.el("td", { "data-label": "Applicant", text: a.full_name }),
          ESH.el("td", { "data-label": "Status" }, [ESH.statusBadge(a.status)]),
          ESH.el("td", { "data-label": "Submitted", text: ESH.fmtDate(a.created_at) }),
        ])
      );
      recentEl.appendChild(
        ESH.el("div", { class: "table-wrap" }, [
          ESH.el("table", { class: "data" }, [
            ESH.el("thead", {}, [ESH.el("tr", {}, [
              ESH.el("th", { text: "Reference" }),
              ESH.el("th", { text: "Service" }),
              ESH.el("th", { text: "Applicant" }),
              ESH.el("th", { text: "Status" }),
              ESH.el("th", { text: "Submitted" }),
            ])]),
            ESH.el("tbody", {}, rows),
          ]),
        ])
      );
      }
    }
    loadActivity();
  }

  document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("refreshBtn")?.addEventListener("click", () => window.location.reload());
    document.getElementById("activityPagination")?.addEventListener("click", (event) => {
      const button = event.target.closest("[data-activity-page]");
      if (button && !button.disabled) loadActivity(Number(button.dataset.activityPage));
    });
    if (document.body.dataset.page === "staff-dashboard") init();
  });
})();
