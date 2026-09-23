/* eSHCAT — emergency.js
   Public "Emergency & Important Numbers" card on the homepage.
   Renders the DB-backed list; keeps the static markup as an offline fallback. */

(function () {
  function renderHome() {
    const container = document.getElementById("emergencyContacts");
    if (!container || !window.ESH.api) return;
    window.ESH.api
      .get("/emergency-numbers")
      .then((rows) => {
        if (!rows || !rows.length) return;
        container.innerHTML = "";
        rows.forEach((r) => {
          const item = document.createElement("div");
          item.className = "contact-item";
          const k = document.createElement("div");
          k.className = "k";
          k.textContent = r.label;
          const v = document.createElement("div");
          v.className = "v";
          v.textContent = r.value;
          item.appendChild(k);
          item.appendChild(v);
          container.appendChild(item);
        });
      })
      .catch(() => {
        /* keep static fallback content */
      });
  }

  document.addEventListener("DOMContentLoaded", () => {
    if (document.body.dataset.page === "home") renderHome();
  });
})();