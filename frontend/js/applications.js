/* eSHCAT — applications.js
   Dynamic application form built from the service's form fields.
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
      ESH.showEmpty(container, err.message);
      return;
    }

    if (!svc.is_online) {
      ESH.showEmpty(container, "Online application is currently unavailable for this service.", `<a class="btn btn-secondary" href="/pages/offices.html">View Office Information</a>`);
      return;
    }

    document.getElementById("serviceName") && (document.getElementById("serviceName").textContent = svc.name);
    document.getElementById("serviceDept") && (document.getElementById("serviceDept").textContent = svc.department);

    const fields = svc.form_fields || [svc];
    const form = ESH.el("form", { id: "applicationForm", novalidate: "" });
    const grid = ESH.el("div", { class: "form-grid" });

    fields.forEach((f) => {
      const group = ESH.el("div", { class: "form-group" });
      const req = f.required ? '<span class="req">*</span>' : "";
      switch (f.field_type) {
        case "textarea":
          group.appendChild(ESH.el("label", { for: f.field_name }, [f.label, reqMark(f.required)]));
          group.appendChild(ESH.el("textarea", { id: f.field_name, name: f.field_name, rows: "3" }));
          if (!f.required) group.appendChild(ESH.el("div", { class: "form-error", id: `err-${f.field_name}` }));
          break;
        case "select":
          group.appendChild(ESH.el("label", { for: f.field_name }, [f.label, reqMark(f.required)]));
          const select = ESH.el("select", { id: f.field_name, name: f.field_name });
          (f.options || []).forEach((o) => select.appendChild(ESH.el("option", { value: o, text: o })));
          group.appendChild(select);
          if (!f.required) group.appendChild(ESH.el("div", { class: "form-error", id: `err-${f.field_name}` }));
          break;
        default:
          group.appendChild(ESH.el("label", { for: f.field_name }, [f.label, reqMark(f.required)]));
          group.appendChild(ESH.el("input", { id: f.field_name, name: f.field_name, type: f.field_type || "text", required: f.required ? "required" : undefined }));
          if (!f.required) group.appendChild(ESH.el("div", { class: "form-error", id: `err-${f.field_name}` }));
      }
      grid.appendChild(group);
    });

    // Standard applicant fields appended for every service
    const applicantFields = [
      { label: "Full Name", name: "fullName", type: "text", required: true },
      { label: "Email Address", name: "email", type: "email", required: true },
      { label: "Mobile Number", name: "mobile", type: "tel", required: false },
      { label: "Address", name: "address", type: "text", required: true },
    ];
    applicantFields.forEach((f) => {
      const group = ESH.el("div", { class: "form-group" }, [
        ESH.el("label", { for: f.name }, [f.label, reqMark(f.required)]),
        ESH.el("input", { id: f.name, name: f.name, type: f.type, required: f.required ? "required" : undefined }),
        ESH.el("div", { class: "form-error", id: `err-${f.name}` }),
      ]);
      grid.appendChild(group);
    });

    const disclaimer = ESH.el("div", { class: "alert warning" }, [
      ESH.el("span", { text: "⚠ This is a hackathon prototype. No real government records are used. Details are demo data." }),
    ]);
    grid.appendChild(ESH.el("div", { class: "form-group full" }, [disclaimer]));

    const actions = ESH.el("div", { class: "form-group full", style: "display:flex;gap:10px;flex-wrap:wrap" }, [
      ESH.el("a", { class: "btn btn-secondary", href: "/pages/services.html", text: "Cancel" }),
      ESH.el("button", { class: "btn btn-primary", type: "submit", id: "submitBtn", text: "Submit Application →" }),
    ]);
    grid.appendChild(actions);

    form.appendChild(grid);
    container.innerHTML = "";
    container.appendChild(form);

    form.addEventListener("submit", onSubmit);
  }

  function reqMark(required) {
    return required ? " " + ESH.el("span", { class: "req", text: "*" }) : ESH.el("span", { text: " (optional)" });
  }

  let submitting = false;

  async function onSubmit(event) {
    event.preventDefault();
    const form = event.target;
    if (submitting) return;

    const errs = form.querySelectorAll(".form-error");
    errs.forEach((e) => (e.textContent = ""));

    const values = new FormData(form);
    const data = {};
    values.forEach((v, k) => (data[k] = v));

    // Validate
    const requiredFields = ["fullName", "email", "address"];
    let firstError = null;
    for (const name of requiredFields) {
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
      return;
    }

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
      showSuccess(response.reference_number, ESH.getParam("id"));
    } catch (err) {
      if (err.network || !navigator.onLine) {
        // Save draft locally + queue for sync
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
      ESH.el("div", { style: "font-size:2.4rem;margin-bottom:8px", text: "✓" }),
      ESH.el("h2", { text: "Application Submitted" }),
      ESH.el("p", { class: "note", text: "Your request has been recorded." }),
      ESH.el("div", { class: "ref-box" }, [
        ESH.el("div", { class: "k", style: "text-transform:uppercase;font-size:0.8rem;color:var(--color-text-muted);font-weight:700", text: "Reference Number" }),
        ESH.el("div", { class: "ref", text: reference }),
        ESH.el("div", { class: "hint", text: "Save this number to track your application." }),
      ]),
      ESH.el("a", { class: "btn btn-primary", href: `/pages/track.html?ref=${encodeURIComponent(reference)}`, text: "Track Application →" }),
    ]);
    container.appendChild(box);
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