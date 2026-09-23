/* eSHCAT — offices.js
   Municipal office directory (from /api/offices, with a static
   fallback) — search, filters, and office cards. */

(function () {
  const ESH = window.ESH;

  const FALLBACK = [
    { name: "Local Civil Registry Office (LCRO)", contact_number: "(055) 500-0712", location: "Municipal Hall, Catarman, Northern Samar", office_hours: "Mon-Fri 8:00 AM - 5:00 PM", services: [{ name: "Death Certificate" }, { name: "Birth Certificate" }, { name: "Marriage Certificate" }] },
    { name: "Business Permits and Licensing Office (BPLO)", contact_number: "(055) 500-0712", location: "Municipal Hall, Catarman, Northern Samar", office_hours: "Mon-Fri 8:00 AM - 5:00 PM", services: [{ name: "New / Renew Business Permit" }] },
    { name: "Municipal Treasurer's Office (MTO)", contact_number: "(055) 500-1453", location: "Municipal Hall, Catarman, Northern Samar", office_hours: "Mon-Fri 8:00 AM - 5:00 PM", services: [{ name: "Community Tax Certificate (Cedula)" }] },
    { name: "Municipal Assessor's Office (MASSO)", contact_number: "(055) 500-2042", location: "Municipal Hall, Catarman, Northern Samar", office_hours: "Mon-Fri 8:00 AM - 5:00 PM", services: [{ name: "Transfer of Ownership of Real Property" }] },
  ];

  function officeCard(o) {
    const services = (o.services || []).map((s) => s.name).slice(0, 4);
    const items = [
      o.location && ESH.el("li", { text: `📍 ${o.location}` }),
      o.contact_number && ESH.el("li", { text: `📞 ${o.contact_number}` }),
      o.email && ESH.el("li", { text: `✉️ ${o.email}` }),
      o.office_hours && ESH.el("li", { text: `🕘 ${o.office_hours}` }),
    ].filter(Boolean);
    return ESH.el("div", { class: "card" }, [
      ESH.el("div", { class: "icon-tile", text: "🏢" }),
      ESH.el("h3", { text: o.name }),
      ESH.el("ul", { class: "note", style: "margin:8px 0 12px;padding-left:0;list-style:none" }, items),
      services.length
        ? ESH.el("p", { class: "note", style: "font-size:0.84rem;margin-bottom:12px" }, [
            ESH.el("span", { style: "font-weight:600;color:var(--color-text);margin-right:6px", text: "Services:" }),
            ESH.el("span", { text: services.join(", ") + (o.services.length > 4 ? "…" : "") }),
          ])
        : "",
      ESH.el("div", { style: "display:flex;gap:10px;flex-wrap:wrap" }, [
        ESH.el("a", { class: "btn btn-sm btn-primary", href: `/pages/services.html?q=${encodeURIComponent(o.name.split(" (")[0])}`, text: "View Services" }),
        o.location
          ? ESH.el("a", { class: "btn btn-sm btn-secondary", href: `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(o.location + ", Catarman, Northern Samar")}`, target: "_blank", rel: "noopener", text: "View Map" })
          : "",
      ]),
    ]);
  }

  async function loadOffices() {
    const data = await ESH.api.get("/offices");
    return Array.isArray(data) && data.length
      ? data.map((o) => ({ ...o, services: o.services || [] }))
      : FALLBACK;
  }

  async function renderPreview() {
    const container = document.getElementById("officesHome");
    if (!container) return;
    ESH.showLoading(container, "Loading offices...");
    let offices = [];
    try {
      offices = await loadOffices();
    } catch (err) {
      offices = FALLBACK; /*
      // Fallback data so the page still works offline-ish
      offices = [
        { name: "Civil Registry", services: ["Birth Certificate", "Marriage Certificate", "Death Certificate"] },
        { name: "BPLO", services: ["New / Renew Business Permit"] },
        { name: "Municipal Treasurer", services: ["Community Tax Certificate (Cedula)"] },
        { name: "Municipal Assessor", services: ["Transfer of Ownership of Real Property"] },
      ];
      */ /*
      ESH.showEmpty(container, err.message);
      return;
      */
    }
    container.innerHTML = "";
    const grid = ESH.el("div", { class: "bento-grid" });
    const picked = offices.filter((o) => o.services && o.services.length).slice(0, 3);
    (picked.length ? picked : offices.slice(0, 3)).forEach((o) => {
      grid.appendChild(ESH.el("div", { class: "bento-4" }, [officeCard(o)]));
    });
    container.appendChild(grid);
  }

  async function renderDirectory() {
    const container = document.getElementById("officeList");
    const searchEl = document.getElementById("officeSearch");
    const chipsEl = document.getElementById("officeChips");
    if (!container) return;

    ESH.showLoading(container, "Loading offices...");
    let offices = [];
    try {
      offices = await loadOffices();
    } catch (err) {
      officeFallback(container);
      return;
    }

    if (chipsEl) {
      chipsEl.appendChild(ESH.el("button", { type: "button", class: "chip active", "data-cat": "", text: "All Offices" }));
      offices.forEach((o) => {
        chipsEl.appendChild(ESH.el("button", { type: "button", class: "chip", "data-cat": o.name, text: o.name }));
      });
    }

    let activeCat = "";
    const draw = () => {
      const q = (searchEl ? searchEl.value : "").trim().toLowerCase();
      const filtered = offices.filter((o) => {
        const matchCat = !activeCat || o.name === activeCat;
        const hay = `${o.name} ${o.location || ""} ${o.contact_number || ""} ${(o.services || []).map((s) => s.name).join(" ")}`.toLowerCase();
        const matchQ = !q || hay.includes(q);
        return matchCat && matchQ;
      });
      if (!filtered.length) {
        ESH.showEmpty(container, "No offices match your search.", `<button class="btn btn-secondary" id="clearOfficeFiltersBtn" type="button">Clear Filters</button>`);
        container.querySelector("#clearOfficeFiltersBtn")?.addEventListener("click", clearFilters);
        return;
      }
      container.innerHTML = "";
      const grid = ESH.el("div", { class: "bento-grid" });
      filtered.forEach((o) => {
        grid.appendChild(ESH.el("div", { class: "bento-4" }, [officeCard(o)]));
      });
      container.appendChild(grid);
    };

    const clearFilters = () => {
      if (searchEl) searchEl.value = "";
      activeCat = "";
      chipsEl?.querySelectorAll(".chip").forEach((c) => c.classList.remove("active"));
      chipsEl?.querySelector('[data-cat=""]')?.classList.add("active");
      draw();
    };

    draw("");
    searchEl?.addEventListener("input", draw);
    chipsEl?.addEventListener("click", (e) => {
      const chip = e.target.closest(".chip");
      if (!chip) return;
      chipsEl.querySelectorAll(".chip").forEach((c) => c.classList.remove("active"));
      chip.classList.add("active");
      activeCat = chip.dataset.cat || "";
      draw();
    });
  }

  function officeFallback(container) {
    container.innerHTML = "";
    const grid = ESH.el("div", { class: "bento-grid" });
    FALLBACK.forEach((o) => {
      grid.appendChild(ESH.el("div", { class: "bento-4" }, [officeCard(o)]));
    });
    container.appendChild(grid);
    ESH.showToast("Showing offline directory.", "info", 3500);
  }

  document.addEventListener("DOMContentLoaded", () => {
    if (document.body.dataset.page === "home") renderPreview();
    if (document.body.dataset.page === "offices") renderDirectory();
  });
})();
