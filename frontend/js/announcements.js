/* eSHCAT — announcements.js
   Public announcements listing. */

(function () {
  const ESH = window.ESH;

  function renderHomeAnnouncements() {
    const container = document.getElementById("announcementsHome");
    if (!container) return;
    load(container, 2);
  }

  function renderAnnouncementsPage() {
    const container = document.getElementById("announcementsList");
    if (container) load(container);
  }

  async function load(container, limit = 0) {
    ESH.showLoading(container, "Loading announcements...");
    let items = [];
    try {
      items = await ESH.api.get("/announcements");
    } catch (err) {
      ESH.showEmpty(container, err.message);
      return;
    }

    const shown = limit ? items.slice(0, limit) : items;
    if (!shown.length) {
      ESH.showEmpty(container, "No announcements right now.");
      return;
    }

    container.innerHTML = "";
    shown.forEach((a) => {
      const item = ESH.el("div", { class: `announcement${a.is_pinned ? " pinned" : ""}` }, [
        a.is_pinned ? ESH.el("span", { class: "badge pending", text: "IMPORTANT" }) : "",
        ESH.el("h3", { style: "margin-top:6px", text: a.title }),
        ESH.el("div", { class: "meta", text: `${a.date_text || ESH.fmtDate(a.created_at)} · ${a.department || "Municipal Government"}` }),
        ESH.el("p", { text: a.description || "" }),
      ]);
      container.appendChild(item);
    });
  }

  document.addEventListener("DOMContentLoaded", () => {
    if (document.body.dataset.page === "announcements") renderAnnouncementsPage();
  });
})();