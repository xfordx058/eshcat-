/* eSHCAT — staff/dashboard.js
   Staff dashboard: bento stat cards, CSS bar chart, quick summary,
   recent applications table. */

(function () {
  const ESH = window.ESH;

  const TILES = [
    { key: "total", label: "Total Applications", icon: "🗂️", tone: "info" },
    { key: "Pending", label: "Pending Review", icon: "⏳", tone: "warning" },
    { key: "Under Review", label: "Under Review", icon: "🔄", tone: "purple" },
    { key: "Completed", label: "Completed", icon: "✅", tone: "success" },
    { key: "appointments", label: "Pending Appointments", icon: "📅", tone: "info" },
    { key: "reports", label: "Open Reports", icon: "🚩", tone: "danger" },
  ];

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
        ESH.el("div", { class: `stat-icon ${t.tone}`, text: t.icon }),
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
        return;
      }
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

  document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("refreshBtn")?.addEventListener("click", () => window.location.reload());
    if (document.body.dataset.page === "staff-dashboard") init();
  });
})();