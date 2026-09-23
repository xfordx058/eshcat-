/* eSHCAT — tracking.js
   Public request tracking by reference number. */

(function () {
  const ESH = window.ESH;

  // Fill reference from URL param if present (e.g. ?ref=CAT-...)
  function prefill() {
    const ref = ESH.getParam("ref");
    const input = document.getElementById("referenceInput");
    if (ref && input) {
      input.value = ref;
      track(ref);
    }
  }

  async function track(reference) {
    const resultEl = document.getElementById("trackResult");
    const input = document.getElementById("referenceInput");

    if (!reference) {
      ESH.showToast("Please enter your reference number.", "error");
      return;
    }
    if (resultEl) {
      ESH.showLoading(resultEl, "Tracking your request...");
    }

    try {
      const data = await ESH.api.get(`/track/${encodeURIComponent(reference)}`);
      renderResult(resultEl, data);
    } catch (err) {
      if (resultEl) {
        ESH.showEmpty(resultEl, err.message);
      }
    } finally {
      if (input) input.disabled = false;
    }
  }

  function renderResult(container, data) {
    container.innerHTML = "";

    const card = ESH.el("div", { class: "card", style: "max-width:640px;margin:0 auto" }, [
      ESH.el("div", { style: "display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:10px" }, [
        ESH.el("div", { class: "dept", text: data.department }),
        ESH.statusBadge(data.status),
      ]),
      ESH.el("h2", { style: "font-size:1.4rem", text: data.service }),
      ESH.el("div", { class: "detail-list", style: "margin:14px 0 6px" }, [
        ESH.el("div", { class: "detail-item" }, [
          ESH.el("div", { class: "k", text: "Reference Number" }),
          ESH.el("div", { class: "v", text: data.reference_number }),
        ]),
        ESH.el("div", { class: "detail-item" }, [
          ESH.el("div", { class: "k", text: "Last Updated" }),
          ESH.el("div", { class: "v", text: ESH.fmtDateTime(data.updated_at) }),
        ]),
        ESH.el("div", { class: "detail-item" }, [
          ESH.el("div", { class: "k", text: "Submitted" }),
          ESH.el("div", { class: "v", text: ESH.fmtDateTime(data.created_at || data.updated_at) }),
        ]),
      ]),
    ]);

    const privacy = ESH.el("p", { class: "note", style: "max-width:640px;margin:14px auto 0;font-size:0.82rem;text-align:center" }, [
      ESH.el("span", { text: "" }, [ESH.el("i", { class: "fa-solid fa-lock", "aria-hidden": "true" }), " Privacy: tracking shows \"status only\" — no personal records are displayed." ]),
    ]);

    const timelineCard = ESH.el("div", { class: "card", style: "max-width:640px;margin:18px auto 0" }, [
      ESH.el("h3", { text: "Application Timeline" }),
      buildTimeline((data.timeline || []).reverse()),
    ]);

    container.appendChild(card);
    container.appendChild(privacy);
    container.appendChild(timelineCard);
  }

  function buildTimeline(entries) {
    const ul = ESH.el("ul", { class: "timeline" });
    entries.forEach((entry, idx) => {
      const isCurrent = idx === entries.length - 1;
      const li = ESH.el("li", { class: isCurrent ? "current" : "done" }, [
        ESH.el("span", { class: "status-name", text: entry.new_status || entry.old_status }),
        ESH.el("div", { class: "time", text: ESH.fmtDateTime(entry.created_at) }),
        entry.remarks ? ESH.el("div", { class: "note", text: entry.remarks }) : "",
      ]);
      ul.appendChild(li);
    });
    return ul;
  }

  document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("trackForm");
    if (form) {
      form.addEventListener("submit", (e) => {
        e.preventDefault();
        const input = document.getElementById("referenceInput");
        track(input ? input.value.trim() : "");
      });
    }
    if (document.body.dataset.page === "track") prefill();
  });
})();