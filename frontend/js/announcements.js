/* eSHCAT — announcements.js
   Public announcements listing (home preview + full page with filters). */

(function () {
  const ESH = window.ESH;

  function matchesFilter(item, cat) {
    if (!cat) return true;
    const hay = `${item.title} ${item.department || ""} ${item.description || ""}`.toLowerCase();
    switch (cat) {
      case "Government":
        return /mayor|government|executive|municipal|general/i.test(hay);
      case "Services":
        return /civil registry|bplo|business|permit|treasurer|assessor|building|zoning|office|health|social|registration|tax|service|payment/i.test(hay);
      case "Health":
        return /health|rhu|medical|free|check-?up|immunization|dengue/i.test(hay);
      case "Emergency":
        return /emergency|advisory|typhoon|flood|disaster|mdrrmo|weather|storm|evacuation|heat/i.test(hay);
      default:
        return true;
    }
  }

  function renderHomeAnnouncements() {
    const container = document.getElementById("announcementsHome");
    if (!container) return;
    load(container, 3, "");
  }

  function renderAnnouncementsPage() {
    const container = document.getElementById("announcementsList");
    if (!container) return;

    const chips = document.getElementById("announcementChips");
    let activeCat = "";
    if (chips) {
      chips.addEventListener("click", (e) => {
        const chip = e.target.closest(".chip");
        if (!chip) return;
        chips.querySelectorAll(".chip").forEach((c) => c.classList.remove("active"));
        chip.classList.add("active");
        activeCat = chip.dataset.cat || "";
        load(container, 0, activeCat);
      });
    }
    load(container, 0, "");
  }

  async function load(container, limit, cat) {
    ESH.showLoading(container, "Loading announcements...");
    let items = [];
    try {
      items = await ESH.api.get("/announcements");
    } catch (err) {
      ESH.showError(container, err.message, () => load(container, limit, cat));
      return;
    }

    const filtered = items.filter((a) => matchesFilter(a, cat));
    const shown = limit ? filtered.slice(0, limit) : filtered;
    if (!shown.length) {
      ESH.showEmpty(container, "No announcements in this category right now.", `<button class="btn btn-secondary" id="clearAnnouncementsBtn" type="button">Clear Filters</button>`);
      container.querySelector("#clearAnnouncementsBtn")?.addEventListener("click", () => {
        const chips = document.getElementById("announcementChips");
        chips?.querySelectorAll(".chip").forEach((c) => c.classList.remove("active"));
        chips?.querySelector('[data-cat=""]')?.classList.add("active");
        load(container, limit, "");
      });
      return;
    }

    container.innerHTML = "";
    shown.forEach((a) => {
      const desc = a.description || "";
      const item = ESH.el("div", { class: `announcement${a.is_pinned ? " pinned" : ""}` }, [
        a.is_pinned ? ESH.el("span", { class: "pin-badge", text: "📌 Pinned" }) : "",
        ESH.el("h3", { text: a.title }),
        ESH.el("div", { class: "meta", text: `${a.date_text || ESH.fmtDate(a.created_at)} · ${a.department || "Municipal Government"}` }),
        ESH.el("p", { class: `body${limit ? " clamped" : ""}`, text: desc }),
      ]);
      container.appendChild(item);
    });
  }

  document.addEventListener("DOMContentLoaded", () => {
    if (document.body.dataset.page === "home") renderHomeAnnouncements();
    if (document.body.dataset.page === "announcements") renderAnnouncementsPage();
  });
})();