/* eSHCAT — reports.js
   Community report submissions. */

(function () {
  const ESH = window.ESH;

  function init() {
    const form = document.getElementById("reportForm");
    if (!form) return;

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      const btn = document.getElementById("reportBtn");
      const photoFile = document.getElementById("reportPhoto")?.files?.[0];
      const payload = {
        category: document.getElementById("reportCategory").value,
        location: document.getElementById("reportLocation").value,
        description: document.getElementById("reportDescription").value,
        name: document.getElementById("reportName").value,
        email: document.getElementById("reportEmail").value,
      };

      if (!payload.category || !payload.description.trim()) {
        ESH.showToast("Category and description are required.", "error");
        return;
      }

      ESH.setLoading(btn, "Submitting...");
      try {
        if (photoFile) payload.photo_data = await readFileAsDataUrl(photoFile);
        const data = await ESH.api.post("/reports", payload);
        const formContainer = form.parentElement;
        form.remove();
        const card = ESH.el("div", { class: "card", style: "max-width:560px;margin:0 auto;text-align:center" }, [
          ESH.el("div", { style: "font-size:2.4rem;color:var(--color-success)", text: "" }, [ESH.el("i", { class: "fa-solid fa-circle-check", "aria-hidden": "true" })]),
          ESH.el("h2", { text: "Report Submitted" }),
          ESH.el("p", { class: "note", text: "Thank you. Your concern has been recorded." }),
          ESH.el("div", { class: "ref-box" }, [
            ESH.el("div", { class: "ref", text: data.reference_number }),
          ]),
          ESH.el("a", { class: "btn btn-secondary", href: "/", text: "Back to Home" }),
        ]);
        formContainer.appendChild(card);
        ESH.showToast("Report submitted.", "success");
      } catch (err) {
        ESH.unsetLoading(btn);
        ESH.showToast(err.message, "error");
      }
    });
  }

  function readFileAsDataUrl(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result);
      reader.onerror = () => reject(new Error("The selected photo could not be read."));
      reader.readAsDataURL(file);
    });
  }

  document.addEventListener("DOMContentLoaded", () => {
    if (document.body.dataset.page === "reports") init();
  });
})();
