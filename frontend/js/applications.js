/* eSHCAT — applications.js
   Dynamic application form built from the service's form fields.
   4-step wizard: Information → Requirements → Review → Submit.
   Supports offline -> local draft -> queue -> sync. */

(function () {
  const ESH = window.ESH;

  async function renderApplyForm() {
    const container = document.getElementById("applyForm");
    if (!container) return;

    const id = ESH.getParam("id");
    if (!id) {
      window.location.href = "/pages/services.html";
      return;
    }

    ESH.showLoading(container, "Loading application form...");
    let svc;
    try {
      svc = await ESH.api.get(`/services/${id}`);
    } catch (err) {
      ESH.showError(container, err.message, () => renderApplyForm());
      return;
    }

    if (!svc.is_online) {
      ESH.showEmpty(container, "Online application is currently unavailable for this service.", `<a class="btn btn-secondary" href="/pages/offices.html">View Office Information</a>`);
      return;
    }

    const nameEl = document.getElementById("serviceName");
    const deptEl = document.getElementById("serviceDept");
    const descEl = document.getElementById("serviceDesc");
    if (nameEl) nameEl.textContent = svc.name;
    if (deptEl) deptEl.textContent = svc.department;
    if (descEl) {
      descEl.textContent = svc.short_description || svc.description || "Fill in the required information. You will receive a reference number after submission.";
    }

    const fields = svc.form_fields || [svc];
    const requirements = Array.isArray(svc.requirements) ? svc.requirements : [];

    container.innerHTML = "";

    const form = ESH.el("form", { id: "applicationForm", novalidate: "", class: "glass-card", style: "max-width:720px;margin:0 auto;padding:28px" });

    /* --- Step 1: Information --- */
    const stepInfo = ESH.el("div", { "data-wizard-panel": "1" });
    const grid = ESH.el("div", { class: "form-grid" });

    fields.forEach((f) => {
      const group = ESH.el("div", { class: "form-group" });
      switch (f.field_type) {
        case "textarea":
          group.appendChild(ESH.el("label", { for: f.field_name }, [f.label, reqMark(f.required)]));
          group.appendChild(ESH.el("textarea", { id: f.field_name, name: f.field_name, rows: "3" }));
          group.appendChild(ESH.el("div", { class: "form-error", id: `err-${f.field_name}` }));
          break;
        case "select":
          group.appendChild(ESH.el("label", { for: f.field_name }, [f.label, reqMark(f.required)]));
          const select = ESH.el("select", { id: f.field_name, name: f.field_name });
          select.appendChild(ESH.el("option", { value: "", text: "Select..." }));
          (f.options || []).forEach((o) => select.appendChild(ESH.el("option", { value: o, text: o })));
          group.appendChild(select);
          group.appendChild(ESH.el("div", { class: "form-error", id: `err-${f.field_name}` }));
          break;
        default:
          group.appendChild(ESH.el("label", { for: f.field_name }, [f.label, reqMark(f.required)]));
          group.appendChild(ESH.el("input", { id: f.field_name, name: f.field_name, type: f.field_type || "text" }));
          group.appendChild(ESH.el("div", { class: "form-error", id: `err-${f.field_name}` }));
      }
      grid.appendChild(group);
    });

    const applicantFields = [
      { label: "Full Name", name: "fullName", type: "text", placeholder: "e.g. Juan A. Dela Cruz", required: true },
      { label: "Email Address", name: "email", type: "email", placeholder: "you@example.com", required: true },
      { label: "Mobile Number", name: "mobile", type: "tel", placeholder: "09xx-xxx-xxxx (optional)", required: false },
      { label: "Address", name: "address", type: "text", placeholder: "e.g. Purok 3, Brgy. 1, Catarman", required: true },
    ];
    applicantFields.forEach((f) => {
      const group = ESH.el("div", { class: "form-group" }, [
        ESH.el("label", { for: f.name }, [f.label, reqMark(f.required)]),
        ESH.el("input", { id: f.name, name: f.name, type: f.type, placeholder: f.placeholder || "" }),
        ESH.el("div", { class: "form-error", id: `err-${f.name}` }),
      ]);
      grid.appendChild(group);
    });

    stepInfo.appendChild(grid);
    stepInfo.appendChild(
      ESH.el("div", { class: "form-group full", style: "display:flex;gap:10px;justify-content:flex-end;margin-top:20px" }, [
        ESH.el("a", { class: "btn btn-secondary", href: "/pages/services.html", text: "Cancel" }),
        ESH.el("button", { class: "btn btn-primary", type: "button", "data-wizard-next": "2", text: "Continue →" }),
      ])
    );

    /* --- Step 2: Requirements --- */
    const stepReq = ESH.el("div", { "data-wizard-panel": "2", hidden: "hidden" });
    const reqCards = requirements.length
      ? requirements.map((r) =>
          ESH.el("label", {}, [
            ESH.el("input", { type: "checkbox", class: "req-check", "data-required-item": "1" }),
            ESH.el("span", { text: r }),
          ])
        )
      : [ESH.el("p", { class: "note", text: "See the office for the current requirements. For this prototype, no attachments are needed." })];
    stepReq.appendChild(ESH.el("h3", { text: "Required Documents" }));
    stepReq.appendChild(ESH.el("p", { class: "note", text: "Confirm each item below — you will submit them to the office when requested." }));
    stepReq.appendChild(ESH.el("div", { class: "check-list" }, reqCards));
    stepReq.appendChild(
      ESH.el("div", { class: "form-group full", style: "display:flex;gap:10px;justify-content:space-between;margin-top:20px" }, [
        ESH.el("button", { class: "btn btn-secondary", type: "button", "data-wizard-back": "1", text: "← Back" }),
        ESH.el("button", { class: "btn btn-primary", type: "button", "data-wizard-next": "3", text: "Continue →" }),
      ])
    );

    /* --- Step 3: Review --- */
    const stepReview = ESH.el("div", { "data-wizard-panel": "3", hidden: "hidden" });
    const reviewCard = ESH.el("div", { class: "card review-card" }, [
      ESH.el("h3", { text: "Review Your Application" }),
      ESH.el("dl", { id: "reviewList", style: "margin-top:16px" }),
      ESH.el("div", { style: "display:flex;gap:10px;justify-content:space-between;margin-top:18px;flex-wrap:wrap" }, [
        ESH.el("button", { class: "btn btn-secondary", type: "button", "data-wizard-back": "2", text: "← Edit" }),
        ESH.el("button", { class: "btn btn-primary", type: "button", "data-wizard-next": "4", text: "Continue →" }),
      ]),
    ]);
    stepReview.appendChild(reviewCard);

    /* --- Step 4: Submit --- */
    const stepSubmit = ESH.el("div", { "data-wizard-panel": "4", hidden: "hidden" }, [
      ESH.el("div", { class: "card", style: "text-align:center" }, [
        ESH.el("div", { style: "font-size:2.2rem;margin-bottom:6px", text: "📨" }),
        ESH.el("h3", { text: "Ready to Submit?" }),
        ESH.el("p", { class: "note", text: "Double-check your information. Once submitted, you will receive a reference number to track your application." }),
        ESH.el("div", { style: "margin:14px 0" }, [
          ESH.el("div", { class: "alert warning", style: "text-align:left;margin:0" }, [
            ESH.el("span", { text: "This is a hackathon prototype. No real government records are used — details are demo data." }),
          ]),
        ]),
        ESH.el("button", { class: "btn btn-accent", id: "submitBtn", type: "submit", style: "min-width:220px;justify-content:center", text: "Submit Application →" }),
        ESH.el("div", { style: "margin-top:12px" }, [
          ESH.el("button", { class: "btn btn-ghost", type: "button", "data-wizard-back": "3", text: "← Back to review" }),
        ]),
      ]),
    ]);

    form.appendChild(stepInfo);
    form.appendChild(stepReq);
    form.appendChild(stepReview);
    form.appendChild(stepSubmit);
    container.appendChild(form);

    /* --- Wizard navigation --- */
    function showStep(n) {
      form.querySelectorAll("[data-wizard-panel]").forEach((p) => (p.hidden = p.dataset.wizardPanel !== String(n)));
      document.querySelectorAll("[data-wizard-step]").forEach((s, i) => {
        const step = Number(s.dataset.wizardStep);
        s.classList.toggle("active", step === n);
        s.classList.toggle("done", step < n);
      });
      document.querySelectorAll("[data-wizard-connector]").forEach((c) => {
        c.classList.toggle("done", Number(c.dataset.wizardConnector) < n);
      });
      if (n === 3) buildReview();
      window.scrollTo({ top: 0, behavior: "smooth" });
    }

    form.addEventListener("click", (e) => {
      const nextBtn = e.target.closest("[data-wizard-next]");
      if (nextBtn) {
        e.preventDefault();
        const from = Number(nextBtn.dataset.wizardNext) - 1;
        if (validateStep(from)) showStep(Number(nextBtn.dataset.wizardNext));
        return;
      }
      const backBtn = e.target.closest("[data-wizard-back]");
      if (backBtn) {
        e.preventDefault();
        showStep(Number(backBtn.dataset.wizardBack));
      }
    });

    function buildReview() {
      const dl = form.querySelector("#reviewList");
      dl.innerHTML = "";
      const values = new FormData(form);
      const entries = [];
      values.forEach((v, k) => entries.push([labelFor(k), v]));
      entries.forEach(([label, value]) => {
        const dt = ESH.el("dt", { text: label });
        const dd = ESH.el("dd", { text: String(value || "—") });
        dl.appendChild(dt);
        dl.appendChild(dd);
      });
    }

    function labelFor(name) {
      const labelEl = form.querySelector(`label[for="${name}"]`);
      if (labelEl) return labelEl.textContent.replace(/\s*(\(optional\)|\*)\s*$/, "").trim();
      return name.replace(/([A-Z])/g, " $1").trim();
    }

    function validateStep(step) {
      if (step === 1) return validateInfo();
      if (step === 2) return validateRequirements();
      return true;
    }

    function validateInfo() {
      const errs = form.querySelectorAll(".form-error");
      errs.forEach((e) => (e.textContent = ""));
      const values = new FormData(form);
      const data = {};
      values.forEach((v, k) => (data[k] = v));

      let firstError = null;
      const required = ["fullName", "email", "address"];
      for (const name of required) {
        if (!String(data[name] || "").trim()) {
          const errEl = document.getElementById(`err-${name}`);
          if (errEl) errEl.textContent = "This field is required.";
          if (!firstError) firstError = errEl;
        }
      }
      const email = String(data.email || "").trim();
      if (email && !/^\S+@\S+\.\S+$/.test(email)) {
        const errEl = document.getElementById("err-email");
        if (errEl) errEl.textContent = "Enter a valid email address.";
        if (!firstError) firstError = errEl;
      }
      if (firstError) {
        firstError.scrollIntoView({ block: "center", behavior: "smooth" });
        ESH.showToast("Please complete the required fields.", "error");
        return false;
      }
      return true;
    }

    function validateRequirements() {
      const checks = form.querySelectorAll("[data-required-item]");
      if (checks.length && !Array.from(checks).some((c) => c.checked)) {
        ESH.showToast("Please confirm the requirements checklist to continue.", "error");
        return false;
      }
      return true;
    }

    form.addEventListener("submit", onSubmit);
  }

  function reqMark(required) {
    return required ? " *" : ESH.el("span", { class: "req", text: "" });
  }

  let submitting = false;

  async function onSubmit(event) {
    event.preventDefault();
    const form = event.target;
    if (submitting) return;

    const values = new FormData(form);
    const data = {};
    values.forEach((v, k) => (data[k] = v));

    const serviceId = ESH.getParam("id");
    const payload = {
      service_id: Number(serviceId),
      fullName: data.fullName,
      email: data.email,
      mobile: data.mobile || "",
      address: data.address || "",
    };
    const formData = {};
    Object.keys(data).forEach((k) => {
      if (!["fullName", "email", "mobile", "address"].includes(k)) formData[k] = data[k];
    });
    payload.form_data = formData;

    const submitBtn = document.getElementById("submitBtn");
    ESH.setLoading(submitBtn, "Submitting...");
    submitting = true;

    try {
      const response = await ESH.api.post("/applications", payload);
      form.remove();
      showSuccess(response.reference_number);
    } catch (err) {
      if (err.network || !navigator.onLine) {
        const draft = ESH.storage.saveDraft({ ...payload, endpoint: "/applications", serviceId: Number(serviceId) });
        await ESH.storage.enqueueSubmission({ endpoint: "/applications", data: payload, draftId: draft.localId });
        form.remove();
        showOfflineSaved(draft.localId);
      } else {
        ESH.unsetLoading(submitBtn);
        ESH.showToast(err.message, "error");
      }
    } finally {
      submitting = false;
    }
  }

  function showSuccess(reference) {
    const container = document.getElementById("applyForm");
    container.innerHTML = "";
    const box = ESH.el("div", { class: "card", style: "max-width:560px;margin:0 auto;text-align:center" }, [
      ESH.el("div", { style: "font-size:2.6rem;margin-bottom:8px", text: "🎉" }),
      ESH.el("h2", { text: "Application Submitted" }),
      ESH.el("p", { class: "note", text: "Your request has been recorded and is now with the responsible office." }),
      ESH.el("div", { class: "ref-box" }, [
        ESH.el("div", { class: "hint", style: "font-size:0.8rem;text-transform:uppercase;letter-spacing:0.06em;font-weight:700", text: "Reference Number" }),
        ESH.el("div", { class: "ref", text: reference }),
        ESH.el("div", { class: "hint", text: "Save this number to track your application." }),
      ]),
      ESH.el("div", { style: "display:flex;gap:10px;justify-content:center;flex-wrap:wrap" }, [
        ESH.el("a", { class: "btn btn-primary", href: `/pages/track.html?ref=${encodeURIComponent(reference)}`, text: "Track Request →" }),
        ESH.el("button", { class: "btn btn-secondary", id: "printBtn", type: "button", text: "Download / Print" }),
        ESH.el("a", { class: "btn btn-ghost", href: "/", text: "Back to Home" }),
      ]),
    ]);
    container.appendChild(box);
    container.querySelector("#printBtn").addEventListener("click", () => window.print());
    ESH.showToast("Application submitted successfully.", "success");
  }

  function showOfflineSaved(localId) {
    const container = document.getElementById("applyForm");
    container.innerHTML = "";
    const box = ESH.el("div", { class: "card", style: "max-width:560px;margin:0 auto" }, [
      ESH.el("div", { style: "font-size:2.4rem", text: "📴" }),
      ESH.el("h2", { text: "Saved Locally" }),
      ESH.el("p", { class: "note", text: "You are offline. Your application was saved on this device and will be submitted automatically when your connection returns." }),
      ESH.el("div", { class: "ref-box", style: "border-style:solid" }, [
        ESH.el("div", { class: "badge pending", text: "● Pending Sync" }),
      ]),
      ESH.el("p", { class: "note", style: "text-align:center", text: `Draft ID: ${localId}` }),
    ]);
    container.appendChild(box);
    ESH.showToast("Draft saved locally. Will sync when online.", "info", 6000);
  }

  const page = document.body.dataset.page;
  if (page === "apply") renderApplyForm();
})();