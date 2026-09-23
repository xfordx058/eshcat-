/* eSHCAT — offices.js
   Municipal office directory (from /api/services departments +
   static fallback). */

(function () {
  const ESH = window.ESH;

  async function init() {
    const container = document.getElementById("officeList");
    if (!container) return;
    ESH.showLoading(container, "Loading offices...");

    let offices = [];
    try {
      const services = await ESH.api.get("/services");
      const map = new Map();
      services.forEach((s) => {
        const office = { name: s.department, services: [] };
        if (!map.has(s.department_id)) {
          map.set(s.department_id, office);
        }
        map.get(s.department_id).services.push(s.name);
      });
      offices = [...map.values()];
    } catch (err) {
      // Fallback data so the page still works offline-ish
      offices = [
        { name: "Civil Registry", services: ["Birth Certificate", "Marriage Certificate", "E-Death Certificate"] },
        { name: "Business Permits", services: ["Business Permit"] },
        { name: "Municipal Treasurer", services: ["Real Property Tax Inquiry"] },
        { name: "Barangay Affairs", services: ["Barangay Clearance"] },
      ];
    }

    container.innerHTML = "";
    const grid = ESH.el("div", { class: "bento-grid" });
    offices.forEach((o) => {
      const card = ESH.el("div", { class: "card bento-3" }, [
        ESH.el("div", { class: "icon-tile", text: "🏢" }),
        ESH.el("h3", { text: o.name }),
        ESH.el("ul", { class: "note", style: "margin:10px 0 14px;padding-left:18px" }, o.services.map((s) => ESH.el("li", { text: s }))),
        ESH.el("a", { href: "/pages/services.html", text: "Browse services →" }),
      ]);
      grid.appendChild(card);
    });
    container.appendChild(grid);
  }

  document.addEventListener("DOMContentLoaded", () => {
    if (document.body.dataset.page === "offices") init();
  });
})();