/* eSHCAT — services.js
   Home popular services, service directory (search + categories),
   and service detail page (tabs + sticky action panel). */

(function () {
  const ESH = window.ESH;

  const POPULAR_IDS = [1, 2, 6];

  async function renderPopular() {
    const container = document.getElementById("popularServices");
    if (!container) return;
    ESH.showLoading(container, "Loading popular services...");
    let services = [];
    try {
      services = await ESH.api.get("/services");
    } catch (err) {
      ESH.showEmpty(container, err.message);
      return;
    }

    const heroCount = document.getElementById("heroServiceCount");
    if (heroCount) heroCount.textContent = String(services.length);

    const officeCount = document.getElementById("heroOfficeCount");
    if (officeCount && services.length) {
      const depts = new Set(services.map((s) => s.department_id));
      officeCount.textContent = String(depts.size) + "+";
    }

    const chosen = POPULAR_IDS.map((id) => services.find((s) => s.id === id)).filter(Boolean);
    const picked = (chosen.length ? chosen : services.slice(0, 3)).slice(0, 3);
    if (!picked.length) {
      ESH.showEmpty(container, "No services available right now.");
      return;
    }
    container.innerHTML = "";
    picked.forEach((s) => container.appendChild(serviceCard(s)));
  }

  function serviceCard(s) {
    const availability = s.is_online
      ? ESH.el("span", { class: "avail online", text: "● Apply online" })
      : ESH.el("span", { class: "avail offline", text: "● Inquire at office" });
    return ESH.el("a", { class: "card service-card", href: `/pages/service-details.html?id=${s.id}` }, [
      ESH.el("div", { class: "icon-tile" }, [ESH.el("i", { class: "fa-solid fa-landmark", "aria-hidden": "true" })]),
      ESH.el("div", { class: "dept", text: s.department }),
      ESH.el("h3", { text: s.name }),
      ESH.el("p", { text: s.short_description || s.description || "" }),
      ESH.el("div", { class: "meta" }, [
        s.estimated_processing
          ? ESH.el("span", { class: "pill" }, [ESH.el("i", { class: "fa-solid fa-clock", "aria-hidden": "true" }), " ", s.estimated_processing])
          : "",
        availability,
      ]),
      ESH.el("div", { class: "foot" }, [
        ESH.el("span", { text: "View details" }),
        ESH.el("i", { class: "fa-solid fa-arrow-right", "aria-hidden": "true" }),
      ]),
    ]);
  }

  async function renderDirectory() {
    const listEl = document.getElementById("serviceList");
    const searchEl = document.getElementById("serviceSearch");
    const chipsEl = document.getElementById("categoryChips");
    if (!listEl) return;

    ESH.showLoading(listEl, "Loading services...");
    let services = [];
    try {
      services = await ESH.api.get("/services");
    } catch (err) {
      ESH.showEmpty(listEl, err.message);
      return;
    }

    if (chipsEl) {
      const categories = Array.from(
        new Map(services.map((s) => [s.department_id, s.department])).values()
      );
      const allChip = ESH.el("button", { type: "button", class: "chip active", "data-cat": "" }, [
        ESH.el("span", { text: "All Services" }),
        ESH.el("span", { class: "count" }, []),
      ]);
      chipsEl.appendChild(allChip);
      categories.forEach((name) => {
        chipsEl.appendChild(ESH.el("button", { type: "button", class: "chip", "data-cat": name }, [
          ESH.el("span", { text: name }),
        ]));
      });
    }

    let activeCat = "";

    function draw() {
      const q = (searchEl ? searchEl.value : "").trim().toLowerCase();
      const filtered = services.filter((s) => {
        const matchQ =
          !q ||
          s.name.toLowerCase().includes(q) ||
          (s.department || "").toLowerCase().includes(q) ||
          (s.short_description || "").toLowerCase().includes(q) ||
          (s.description || "").toLowerCase().includes(q);
        const matchCat = !activeCat || s.department === activeCat;
        return matchQ && matchCat;
      });

      if (!filtered.length) {
        ESH.showEmpty(
          listEl,
          "No services match your filters.",
          `<button class="btn btn-secondary" id="clearFiltersBtn" type="button">Clear Filters</button>`
        );
        const clearBtn = listEl.querySelector("#clearFiltersBtn");
        if (clearBtn) clearBtn.addEventListener("click", () => {
          if (searchEl) searchEl.value = "";
          activeCat = "";
          chipsEl?.querySelectorAll(".chip").forEach((c) => c.classList.remove("active"));
          chipsEl?.querySelector('[data-cat=""]')?.classList.add("active");
          draw();
        });
        return;
      }

      listEl.innerHTML = "";
      const grid = ESH.el("div", { class: "services-grid" });
      filtered.forEach((s) => grid.appendChild(serviceCard(s)));
      listEl.appendChild(grid);
    }

    draw("");

    const q = ESH.getParam("q");
    if (q && searchEl) {
      searchEl.value = q;
      draw();
    }

    if (searchEl) {
      searchEl.addEventListener("input", draw);
      const form = document.getElementById("serviceSearchForm");
      if (form) form.addEventListener("submit", (e) => e.preventDefault());
    }
    if (chipsEl) {
      chipsEl.addEventListener("click", (e) => {
        const chip = e.target.closest(".chip");
        if (!chip) return;
        chipsEl.querySelectorAll(".chip").forEach((c) => c.classList.remove("active"));
        chip.classList.add("active");
        activeCat = chip.dataset.cat || "";
        draw();
      });
    }
  }

  async function renderDetails() {
    const container = document.getElementById("serviceDetails");
    if (!container) return;

    const id = ESH.getParam("id");
    if (!id) {
      window.location.href = "/pages/services.html";
      return;
    }

    ESH.showLoading(container, "Loading service...");
    let svc;
    try {
      svc = await ESH.api.get(`/services/${id}`);
    } catch (err) {
      ESH.showError(container, err.message, () => renderDetails());
      return;
    }

    const office = svc.office || {};
    container.innerHTML = "";

    const hero = ESH.el("div", { class: "card", style: "margin-bottom:22px" }, [
      ESH.el("p", { class: "note", style: "margin-bottom:12px" }, [
        ESH.el("a", { href: "/", text: "Home" }),
        ESH.el("span", { text: " / " }),
        ESH.el("a", { href: "/pages/services.html", text: "Services" }),
        ESH.el("span", { text: " / " + ESH.esc(svc.name) }),
      ]),
      ESH.el("div", { class: "icon-tile" }, [ESH.el("i", { class: "fa-solid fa-landmark", "aria-hidden": "true" })]),
      ESH.el("div", { class: "dept", text: svc.department }),
      ESH.el("h1", { style: "font-size:1.7rem;margin:4px 0 8px", text: svc.name }),
      ESH.el("p", { class: "note", text: svc.description || svc.short_description || "" }),
      ESH.el("div", { style: "margin-top:14px;display:flex;gap:10px;flex-wrap:wrap" }, [
        svc.estimated_processing
          ? ESH.el("span", { class: "pill" }, [ESH.el("i", { class: "fa-solid fa-clock", "aria-hidden": "true" }), " ", svc.estimated_processing])
          : "",
        svc.is_online
          ? ESH.el("span", { class: "avail online", text: "● Apply online" })
          : ESH.el("span", { class: "avail offline", text: "● Inquire at office" }),
      ]),
    ]);
    container.appendChild(hero);

    const split = ESH.el("div", { class: "split", style: "margin-top:8px" });

    const left = ESH.el("div", {}, [
      ESH.el("div", { class: "tabs", "aria-label": "Service information" }, [
        ESH.el("button", { type: "button", class: "tab-btn active", "data-tab": "requirements", text: "Requirements" }),
        ESH.el("button", { type: "button", class: "tab-btn", "data-tab": "process", text: "Process" }),
        ESH.el("button", { type: "button", class: "tab-btn", "data-tab": "fees", text: "Fees" }),
        ESH.el("button", { type: "button", class: "tab-btn", "data-tab": "office", text: "Office" }),
      ]),
      ESH.el("div", { id: "tab-requirements", class: "tab-panel" }, [
        requirementsPanel(svc.requirements),
      ]),
      ESH.el("div", { id: "tab-process", class: "tab-panel", hidden: "hidden" }, [processPanel()]),
      ESH.el("div", { id: "tab-fees", class: "tab-panel", hidden: "hidden" }, [feesPanel()]),
      ESH.el("div", { id: "tab-office", class: "tab-panel", hidden: "hidden" }, [officePanel(office, svc.estimated_processing)]),
    ]);

    const action = actionPanel(svc, office);
    split.appendChild(left);
    split.appendChild(action);
    container.appendChild(split);

    split.querySelectorAll(".tab-btn").forEach((btn) => {
      btn.addEventListener("click", () => {
        split.querySelectorAll(".tab-btn").forEach((b) => b.classList.remove("active"));
        split.querySelectorAll(".tab-panel").forEach((p) => (p.hidden = true));
        btn.classList.add("active");
        const panel = document.getElementById(`tab-${btn.dataset.tab}`);
        if (panel) panel.hidden = false;
      });
    });
  }

  function requirementsPanel(list) {
    const items = Array.isArray(list) && list.length
      ? list.map((r) => ESH.el("li", { text: r }))
      : [ESH.el("li", { text: "See the office for the current requirements. This is a demo listing." })];
    const ul = ESH.el("ul", { class: "req-list" }, items);
    return ESH.el("div", { class: "card" }, [
      ESH.el("h3", { text: "Required Documents" }),
      ul,
      ESH.el("p", { class: "note", style: "font-size:0.84rem" }, [
        ESH.el("span", { text: "Remember to bring photocopies and the original documents for verification." }),
      ]),
    ]);
  }

  function processPanel() {
    const steps = ["Submit application", "Staff review", "Verification", "Decision"];
    const list = ESH.el("ol", { class: "timeline", style: "margin:0" });
    steps.forEach((s, i) => {
      list.appendChild(ESH.el("li", { class: i === 0 ? "done" : "" }, [
        ESH.el("span", { class: "status-name", text: s }),
      ]));
    });
    return ESH.el("div", { class: "card" }, [
      ESH.el("h3", { text: "Application Process" }),
      list,
      ESH.el("p", { class: "note", style: "margin-top:14px" }, [
        ESH.el("span", { text: "Status updates appear on the tracking page using your reference number." }),
      ]),
    ]);
  }

  function feesPanel() {
    return ESH.el("div", { class: "card" }, [
      ESH.el("h3", { text: "Fees & Payments" }),
      ESH.el("p", { class: "note", text: "Fees are set by the municipal government and vary by service." }),
      ESH.el("ul", { class: "req-list" }, [
        ESH.el("li", { text: "Processing fees are paid at the municipal hall upon release or as directed by the office." }),
        ESH.el("li", { text: "This prototype does not handle payments — no real fees are charged here." }),
      ]),
      ESH.el("p", { class: "note", style: "margin-top:8px;font-size:0.86rem;background:var(--color-warning-bg);padding:12px 14px;border-radius:10px" }, [
        ESH.el("i", { class: "fa-solid fa-triangle-exclamation", "aria-hidden": "true" }),
        ESH.el("span", { text: " This is a hackathon prototype. Verify official fees and requirements with the municipal office." }),
      ]),
    ]);
  }

  function officePanel(office, processing) {
    const items = [
      office.location && ["Office location", office.location],
      office.contact_number && ["Contact number", office.contact_number],
      processing && ["Est. processing", processing],
      office.office_hours && ["Office hours", office.office_hours],
      office.email && ["Email", office.email],
    ]
      .filter(Boolean)
      .map(([k, v]) =>
        ESH.el("div", { class: "detail-item" }, [
          ESH.el("div", { class: "k", text: k }),
          ESH.el("div", { class: "v", text: v }),
        ])
      );
    return ESH.el("div", { class: "card" }, [
      ESH.el("h3", { text: "Office Information" }),
      ESH.el("div", { class: "detail-list", style: "margin:0" }, items),
      office.location ? ESH.el("a", { class: "btn btn-secondary btn-sm", style: "margin-top:14px", href: `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(office.location + ", Catarman, Northern Samar")}`, target: "_blank", rel: "noopener", text: "Open in Maps" }) : "",
    ]);
  }

  function actionPanel(svc, office) {
    const card = ESH.el("div", { class: "card sticky-form" }, [
      ESH.el("h3", { text: "Start your request" }),
      ESH.el("p", { class: "note", style: "margin:8px 0 16px" }, [
        svc.is_online
          ? "Submit this application online. You will receive a reference number for tracking."
          : "Online application is currently unavailable for this service.",
      ]),
    ]);

    if (svc.is_online) {
      card.appendChild(ESH.el("a", { class: "btn btn-primary", style: "width:100%;justify-content:center", href: `/pages/apply.html?id=${svc.id}` }, [
      ESH.el("span", { text: "Apply Online" }),
      ESH.el("i", { class: "fa-solid fa-arrow-right", "aria-hidden": "true" }),
    ]));
    } else {
      card.appendChild(ESH.el("a", { class: "btn btn-secondary", style: "width:100%;justify-content:center", href: "/pages/offices.html", text: "View Office Information" }));
    }

    const saveBtn = ESH.el("button", { class: "btn btn-ghost btn-sm", style: "width:100%;margin-top:8px", type: "button" });
    const paintSave = () => {
      const saved = savedServices().includes(svc.id);
      saveBtn.classList.toggle("btn-ghost", !saved);
      saveBtn.classList.toggle("btn-secondary", saved);
      saveBtn.innerHTML = saved
        ? '<i class="fa-solid fa-bookmark" aria-hidden="true"></i> Saved for later'
        : '<i class="fa-regular fa-bookmark" aria-hidden="true"></i> Save for later';
    };
    saveBtn.addEventListener("click", () => {
      const saved = savedServices().includes(svc.id);
      toggleSaved(svc.id, !saved);
      ESH.showToast(saved ? "Removed from saved services." : "Service saved for later.", saved ? "info" : "success");
      paintSave();
    });
    paintSave();
    card.appendChild(saveBtn);

    card.appendChild(ESH.el("div", { style: "margin-top:16px;padding-top:14px;border-top:1px solid var(--color-border)" }, [
      ESH.el("div", { class: "k", style: "font-size:0.74rem;font-weight:700;text-transform:uppercase;letter-spacing:0.06em;color:var(--color-text-muted)", text: "Questions?" }),
      ESH.el("p", { class: "note", style: "margin-top:6px;font-size:0.86rem" }, [
        ESH.el("span", { text: office.contact_number || "Visit the office" }),
        ESH.el("span", { text: " · " }),
        ESH.el("a", { href: "/pages/offices.html", text: "Office directory" }),
      ]),
    ]));
    return card;
  }

  function savedServices() {
    try {
      return JSON.parse(localStorage.getItem("eshcat.savedServices") || "[]");
    } catch {
      return [];
    }
  }

  function toggleSaved(id, add) {
    let list = savedServices();
    const value = Number(id);
    list = add ? Array.from(new Set([...list, value])) : list.filter((x) => x !== value);
    localStorage.setItem("eshcat.savedServices", JSON.stringify(list));
  }

  const page = document.body.dataset.page;
  if (page === "home") renderPopular();
  if (page === "services") renderDirectory();
  if (page === "service-details") renderDetails();
})();