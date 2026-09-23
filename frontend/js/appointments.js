/* eSHCAT — appointments.js
   Appointment requests. */

(function () {
  const ESH = window.ESH;

  async function seedOfficeSelect(selectId) {
    const select = document.getElementById(selectId);
    if (!select) return;
    try {
      const services = await ESH.api.get("/services");
      const departments = {};
      services.forEach((s) => (departments[s.department_id] = s.department));
      Object.entries(departments).forEach(([id, name]) => {
        const option = ESH.el("option", { value: id, text: name });
        select.appendChild(option);
      });
    } catch {
      /* leave empty; backend validates */
    }
  }

  async function init() {
    const form = document.getElementById("appointmentForm");
    if (!form) return;
    await seedOfficeSelect("deptSelect");

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      const btn = document.getElementById("apptBtn");
      const payload = {
        department_id: document.getElementById("deptSelect").value || null,
        service_id: null,
        date: document.getElementById("apptDate").value,
        time: document.getElementById("apptTime").value,
        full_name: document.getElementById("apptName").value,
        email: document.getElementById("apptEmail").value,
        mobile: document.getElementById("apptMobile").value,
      };

      ESH.setLoading(btn, "Requesting...");
      try {
        const data = await ESH.api.post("/appointments", payload);
        form.remove();
        const card = ESH.el("div", { class: "card", style: "max-width:560px;margin:0 auto;text-align:center" }, [
          ESH.el("div", { style: "font-size:2.4rem", text: "✓" }),
          ESH.el("h2", { text: "Appointment Requested" }),
          ESH.el("div", { class: "ref-box" }, [
            ESH.el("div", { class: "ref", text: data.reference_number }),
          ]),
          ESH.el("a", { class: "btn btn-secondary", href: "/", text: "Back to Home" }),
        ]);
        form.parentElement.appendChild(card);
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