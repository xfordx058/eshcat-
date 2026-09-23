/* eSHCAT — staff/dashboard.js
   Staff dashboard statistics + recent applications. */

(function () {
  const ESH = window.ESH;

  const TILES = [
    { key: "total", label: "Total Applications", icon: "📄" },
    { key: "Pending", label: "Pending Review", icon: "⏳" },
    { key: "Under Review", label: "Under Review", icon: "🔄" },
    { key: "Completed", label: "Completed", icon: "✅" },
    { key: "appointments", label: "Pending Appointments", icon: "📅" },
    { key: "reports", label: "Open Reports", icon: "🚩" },
  ];

  async function init() {
    const statsEl = document.getElementById("statTiles");
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
      if (statsEl) ESH.showEmpty(statsEl, err.message);
      return;
    }

    if (statsEl) {
      statsEl.innerHTML = "";
      const tiles = ESH.el("div", { class: "stats" });
      TILES.forEach((t) => {
        const value = stats[t.key] ?? 0;
        tiles.appendChild(
          ESH.el("div", { class: "stat" }, [
            ESH.el("div", { class: "label", text: `${t.icon} ${t.label}` }),
            ESH.el("div", { class: "num", text: String(value) }),
          ])
        );
      });
      statsEl.appendChild(tiles);
    }

    if (recentEl) {
      if (!recent.length) {
        ESH.showEmpty(recentEl, "No applications yet.");
        return;
      }
      recentEl.innerHTML = "";
      const list = ESH.el("div", { class: "card", style: "padding:0" }, [
        ESH.el("div", { class: "table-wrap", style: "border:none;box-shadow:none" }, [
          ESH.el("table", { class: "data" }, [
            ESH.el("thead", {}, [ESH.el("tr", {}, [
              ESH.el("th", { text: "Reference" }),
              ESH.el("th", { text: "Service" }),
              ESH.el("th", { text: "Applicant" }),
              ESH.el("th", { text: "Status" }),
              ESH.el("th", { text: "Submitted" }),
            ])]),
            ESH.el("tbody", {}, recent.slice(0, 8).map((a) =>
              ESH.el("tr", {}, [
                ESH.el("td", {}, [ESH.el("a", { href: `/pages/staff/application-details.html?id=${a.id}`, text: a.reference_number })]),
                ESH.el("td", { text: a.service_name }),
                ESH.el("td", { text: a.full_name }),
                ESH.el("td", {}, [ESH.statusBadge(a.status)]),
                ESH.el("td", { text: ESH.fmtDate(a.created_at) }),
              ])
            )),
          ]),
        ]),
      ]);
      recentEl.appendChild(list);
    }
  }

  document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("refreshBtn")?.addEventListener("click", () => window.location.reload());
    if (document.body.dataset.page === "staff-dashboard") init();
  });
})();