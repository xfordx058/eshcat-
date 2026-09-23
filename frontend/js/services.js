/* eSHCAT — services.js
   Service directory + service details page. */

(function () {
  const ESH = window.ESH;

  async function renderDirectory() {
    const listEl = document.getElementById("serviceList");
    const searchEl = document.getElementById("serviceSearch");
    if (!listEl) return;

    ESH.showLoading(listEl, "Loading services...");
    let services = [];
    try {
      services = await ESH.api.get("/services");
    } catch (err) {
      ESH.showEmpty(listEl, err.message);
      return;
    }

    function draw(filter = "") {
      const q = filter.trim().toLowerCase();
      const filtered = services.filter(
        (s) =>
          !q ||
          s.name.toLowerCase().includes(q) ||
          (s.department || "").toLowerCase().includes(q) ||
          (s.short_description || "").toLowerCase().includes(q)
      );

      if (!filtered.length) {
        ESH.showEmpty(listEl, "No services match your search.");
        return;
      }

      listEl.innerHTML = "";
      const grid = ESH.el("div", { class: "bento-grid" });
      filtered.forEach((s) => {
        const card = ESH.el("a", { class: "card", href: `/pages/service-details.html?id=${s.id}` }, [
          ESH.el("div", { class: "icon-tile", text: "🏛" }),
          ESH.el("div", { class: "dept", text: s.department }),
          ESH.el("h3", { text: s.name }),
          ESH.el("p", { text: s.short_description || s.description || "" }),
          ESH.el("div", { class: "foot" }, [
            ESH.el("span", { text: s.is_online ? "Apply online" : "Inquire at office" }),
            ESH.el("span", { text: "View →" }),
          ]),
        ]);
        grid.appendChild(card);
      });
      listEl.appendChild(grid);
    }

    draw("");
    if (searchEl) {
      const q = ESH.getParam("q");
      if (q) {
        searchEl.value = q;
        draw(q);
      }
      searchEl.addEventListener("input", () => draw(searchEl.value));
      const form = document.getElementById("serviceSearchForm");
      if (form) {
        form.addEventListener("submit", (e) => e.preventDefault());
      }
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
      ESH.showEmpty(container, err.message);
      return;
    }

    const office = svc.office || {};
    container.innerHTML = "";
    container.appendChild(
      ESH.el("div", {}, [
        ESH.el("p", { class: "note" }, [
          ESH.el("a", { href: "/pages/services.html", text: "← Back to Services" }),
        ]),
        ESH.el("div", { class: "dept", text: svc.department }),
        ESH.el("h1", { style: "margin:4px 0 10px", text: svc.name }),
        ESH.el("p", { class: "note", text: svc.description || svc.short_description || "" }),
      ])
    );

    const split = ESH.el("div", { class: "split", style: "margin-top:24px" });
    split.appendChild(
      ESH.el("div", {}, [
        requirementsCard(svc.requirements),
        processCard(),
        officeCard(office, svc.estimated_processing),
      ])
    );

    const actionCard = ESH.el("div", { class: "card sticky-form" }, [
      ESH.el("h3", { text: "Start your request" }),
      ESH.el("p", { class: "note", style: "margin:8px 0 16px" }, [
        svc.is_online
          ? "Submit this application online. You will receive a reference number for tracking."
          : "Online application is currently unavailable for this service.",
      ]),
      svc.is_online
        ? ESH.el("a", { class: "btn btn-primary", style: "width:100%;justify-content:center", href: `/pages/apply.html?id=${svc.id}`, text: "Apply Online →" })
        : ESH.el("a", { class: "btn btn-secondary", style: "width:100%;justify-content:center", href: "/pages/offices.html", text: "View Office Information" }),
    ]);
    split.appendChild(actionCard);
    container.appendChild(split);
  }

  function requirementsCard(list) {
    const items = Array.isArray(list) && list.length
      ? list.map((r) => ESH.el("li", { text: r }))
      : [ESH.el("li", { text: "See the office for the current requirements." })];
    const ul = ESH.el("ul", { class: "req-list" }, items);
    return ESH.el("div", { class: "card", style: "margin-bottom:16px" }, [
      ESH.el("h3", { text: "📋 Requirements" }),
      ul,
    ]);
  }

  function processCard() {
    const steps = ["Submit application", "Staff review", "Verification", "Decision"];
    const list = ESH.el("ol", { class: "timeline", style: "margin:0" });
    steps.forEach((s, i) => {
      const li = ESH.el("li", { class: i === 0 ? "done" : "" }, [
        ESH.el("span", { class: "status-name", text: s }),
      ]);
      list.appendChild(li);
    });
    return ESH.el("div", { class: "card", style: "margin-bottom:16px" }, [
      ESH.el("h3", { text: "🔄 Process" }),
      list,
    ]);
  }

  function officeCard(office, processing) {
    const items = [
      office.location && ESH.el("div", { class: "detail-item" }, [ESH.el("div", { class: "k", text: "Office" }), ESH.el("div", { class: "v", text: office.location })]),
      office.contact_number && ESH.el("div", { class: "detail-item" }, [ESH.el("div", { class: "k", text: "Contact" }), ESH.el("div", { class: "v", text: office.contact_number })]),
      processing && ESH.el("div", { class: "detail-item" }, [ESH.el("div", { class: "k", text: "Est. processing" }), ESH.el("div", { class: "v", text: processing })]),
      office.office_hours && ESH.el("div", { class: "detail-item" }, [ESH.el("div", { class: "k", text: "Hours" }), ESH.el("div", { class: "v", text: office.office_hours })]),
    ].filter(Boolean);
    return ESH.el("div", { class: "card" }, [
      ESH.el("h3", { text: "🏢 Office Information" }),
      ESH.el("div", { class: "detail-list", style: "margin:0" }, items),
    ]);
  }

  const page = document.body.dataset.page;
  if (page === "services") renderDirectory();
  if (page === "service-details") renderDetails();
})();