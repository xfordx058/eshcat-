/* eSHCAT — appointments.js
   Appointment requests. */

(function () {
  const ESH = window.ESH;

  async function seedOfficeSelect(selectId, serviceSelectId) {
    const select = document.getElementById(selectId);
    const serviceSelect = document.getElementById(serviceSelectId);
    if (!select) return;
    let services = [];
    try {
      services = await ESH.api.get("/services");
      const departments = {};
      services.forEach((s) => (departments[s.department_id] = s.department));
      Object.entries(departments).forEach(([id, name]) => {
        const option = ESH.el("option", { value: id, text: name });
        select.appendChild(option);
      });
    } catch {
      /* leave empty; backend validates */
    }

    if (serviceSelect) {
      select.addEventListener("change", () => {
        serviceSelect.innerHTML = "";
        serviceSelect.appendChild(ESH.el("option", { value: "", text: "Any service for this office..." }));
        const deptId = select.value;
        services
          .filter((s) => String(s.department_id) === String(deptId))
          .forEach((s) => {
            serviceSelect.appendChild(ESH.el("option", { value: s.id, text: s.name }));
          });
      });
    }
  }

  async function init() {
    const form = document.getElementById("appointmentForm");
    if (!form) return;
    await seedOfficeSelect("deptSelect", "apptService");

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      const btn = document.getElementById("apptBtn");
      const payload = {
        department_id: document.getElementById("deptSelect").value || null,
        service_id: document.getElementById("apptService").value || null,
        date: document.getElementById("apptDate").value,
        time: document.getElementById("apptTime").value,
        full_name: document.getElementById("apptName").value,
        email: document.getElementById("apptEmail").value,
        mobile: document.getElementById("apptMobile").value,
      };

      if (!payload.department_id) {
        ESH.showToast("Please select an office.", "error");
        return;
      }

      ESH.setLoading(btn, "Requesting...");
      try {
        const data = await ESH.api.post("/appointments", payload);
        const formContainer = form.parentElement;
        form.remove();
        const card = ESH.el("div", { class: "card", style: "max-width:560px;margin:0 auto;text-align:center" }, [
          ESH.el("div", { style: "font-size:2.4rem;color:var(--color-success)", text: "" }, [ESH.el("i", { class: "fa-solid fa-circle-check", "aria-hidden": "true" })]),
          ESH.el("h2", { text: "Appointment Requested" }),
          ESH.el("p", { class: "note", text: "Keep your reference number for your visit." }),
          ESH.el("div", { class: "ref-box" }, [
            ESH.el("div", { class: "ref", text: data.reference_number }),
          ]),
          ESH.el("div", { style: "display:flex;gap:10px;justify-content:center;flex-wrap:wrap" }, [
            ESH.el("a", { class: "btn btn-primary", href: "/", text: "Back to Home" }),
            ESH.el("a", { class: "btn btn-secondary", href: "/pages/track.html", text: "Track a Request" }),
          ]),
        ]);
        formContainer.appendChild(card);
        ESH.showToast("Appointment requested.", "success");
      } catch (err) {
        ESH.unsetLoading(btn);
        ESH.showToast(err.message, "error");
      }
    });
  }

  document.addEventListener("DOMContentLoaded", () => {
    if (document.body.dataset.page === "appointments") init();
  });
})();
