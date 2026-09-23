/* eSHCAT — utils.js
   Shared helpers: DOM, toasts, formatting, status classes, modals. */

const API_BASE = "/api";

const PUBLIC_NAV = [
  { href: "/", label: "Home" },
  { href: "/pages/services.html", label: "Services" },
  { href: "/pages/track.html", label: "Track Request" },
  { href: "/pages/appointments.html", label: "Appointments" },
  { href: "/pages/reports.html", label: "Report Concern" },
  { href: "/pages/announcements.html", label: "Announcements" },
  { href: "/pages/offices.html", label: "Offices" },
  { href: "/pages/about.html", label: "About" },
];

const STAFF_NAV = [
  { href: "/pages/staff/dashboard.html", label: '<i class="fa-solid fa-chart-simple" aria-hidden="true"></i> Dashboard' },
  { href: "/pages/staff/applications.html", label: '<i class="fa-solid fa-folder-open" aria-hidden="true"></i> Applications' },
  { href: "/pages/staff/login.html", label: "Logout" },
];

function el(tag, attrs = {}, children = []) {
  const node = document.createElement(tag);
  Object.entries(attrs).forEach(([key, value]) => {
    if (key === "class") node.className = value;
    else if (key === "text") node.textContent = value;
    else if (key.startsWith("on")) node.addEventListener(key.slice(2), value);
    else node.setAttribute(key, value);
  });
  children.forEach((child) => {
    node.appendChild(typeof child === "string" ? document.createTextNode(child) : child);
  });
  return node;
}

function esc(value) {
  const div = document.createElement("div");
  div.textContent = value == null ? "" : String(value);
  return div.innerHTML;
}

function uid() {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 8);
}

function fmtDate(value) {
  if (!value) return "";
  const d = new Date(value + (value.includes("T") ? "" : "T00:00:00"));
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleDateString(undefined, { year: "numeric", month: "short", day: "numeric" });
}

function fmtDateTime(value) {
  if (!value) return "";
  const d = new Date(value.includes("T") ? value : value.replace(" ", "T") + "Z");
  if (Number.isNaN(d.getTime())) return fmtDate(value);
  return d.toLocaleString(undefined, { month: "short", day: "numeric", hour: "numeric", minute: "2-digit" });
}

function statusClass(status) {
  return String(status || "").toLowerCase().replace(/[\s/]+/g, "-");
}

function statusBadge(status) {
  return el("span", { class: `badge ${statusClass(status)}`, text: status || "" });
}

/* Toast notifications */
function showToast(message, type = "info", duration = 4000) {
  let wrap = document.querySelector(".toast-wrap");
  if (!wrap) {
    wrap = el("div", { class: "toast-wrap", "aria-live": "polite" });
    document.body.appendChild(wrap);
  }
  const toast = el("div", { class: `toast ${type}` }, [
    el("span", { text: message }),
  ]);
  wrap.appendChild(toast);
  setTimeout(() => toast.remove(), duration);
}

/* Loading / skeleton helpers */
function showLoading(container, text = "Loading...") {
  container.innerHTML = "";
  const box = el("div", { class: "loading" }, [el("span", { class: "spinner" }), el("span", { text })]);
  container.appendChild(box);
}

function showSkeleton(container, count = 3) {
  container.innerHTML = "";
  for (let i = 0; i < count; i += 1) {
    const card = el("div", { class: "skeleton-card" }, [
      el("div", { class: "skeleton", style: "width:44px;height:44px;border-radius:12px;margin-bottom:14px" }),
      el("div", { class: "skeleton", style: "width:60%;height:18px;margin-bottom:10px" }),
      el("div", { class: "skeleton", style: "width:92%;height:12px;margin-bottom:6px" }),
      el("div", { class: "skeleton", style: "width:74%;height:12px" }),
    ]);
    container.appendChild(card);
  }
}

function showEmpty(container, message, actionHtml = "") {
  const box = el("div", { class: "empty" }, [
    el("div", { class: "big" }, [el("i", { class: "fa-solid fa-inbox", "aria-hidden": "true" })]),
    el("h3", { text: "Nothing here yet" }),
    el("p", { text: message }),
  ]);
  if (actionHtml) box.insertAdjacentHTML("beforeend", actionHtml);
  container.innerHTML = "";
  container.appendChild(box);
}

function showError(container, message, retry) {
  container.innerHTML = "";
  const box = el("div", { class: "empty" }, [
    el("div", { class: "big" }, [el("i", { class: "fa-solid fa-triangle-exclamation", "aria-hidden": "true" })]),
    el("h3", { text: "Something went wrong" }),
    el("p", { text: message }),
  ]);
  if (retry && typeof retry === "function") {
    box.appendChild(el("button", { class: "btn btn-secondary", text: "Try Again", onclick: () => retry() }));
  }
  container.appendChild(box);
}

function setLoading(button, label = "Submitting...") {
  const original = button.dataset.original || button.innerHTML;
  button.dataset.original = original;
  button.disabled = true;
  button.innerHTML = `${label}<span class="spinner" style="width:14px;height:14px;border-width:2px"></span>`;
}

function unsetLoading(button) {
  button.disabled = false;
  if (button.dataset.original) {
    button.innerHTML = button.dataset.original;
    delete button.dataset.original;
  }
}

function getParam(name) {
  return new URLSearchParams(window.location.search).get(name);
}

/* Modal helper */
let activeKeyHandler = null;

function openModal(html, { title = "", onMount } = {}) {
  closeModal();
  const backdrop = el("div", { class: "modal-backdrop", "aria-modal": "true", role: "dialog" });
  backdrop.innerHTML = `<div class="modal">${title ? `<h3>${esc(title)}</h3>` : ""}${html}</div>`;
  document.body.appendChild(backdrop);
  document.body.style.overflow = "hidden";

  const modal = backdrop.querySelector(".modal");
  const close = () => closeModal();
  backdrop.addEventListener("mousedown", (e) => {
    if (e.target === backdrop) close();
  });
  backdrop.addEventListener("click", (e) => {
    if (e.target.closest("[data-close-modal]")) close();
  });

  const onKey = (e) => {
    if (e.key === "Escape") close();
  };
  activeKeyHandler = onKey;
  document.addEventListener("keydown", onKey);

  if (onMount) onMount(modal, backdrop);
  return {
    close,
    modal,
    backdrop,
    el: (sel) => modal.querySelector(sel),
  };
}

function closeModal() {
  const backdrop = document.querySelector(".modal-backdrop");
  if (backdrop) backdrop.remove();
  if (activeKeyHandler) {
    document.removeEventListener("keydown", activeKeyHandler);
    activeKeyHandler = null;
  }
  document.body.style.overflow = "";
}

function confirmModal(message, { title = "Are you sure?", confirmLabel = "Confirm", danger = false } = {}) {
  return new Promise((resolve) => {
    const m = openModal(
      `<p class="modal-sub">${esc(message)}</p>
       <div class="modal-actions">
         <button class="btn btn-secondary" data-close-modal>Cancel</button>
         <button class="btn ${danger ? "btn-danger" : "btn-primary"}" data-confirm>${esc(confirmLabel)}</button>
       </div>`,
      { title }
    );
    m.el("[data-confirm]").addEventListener("click", () => {
      m.close();
      resolve(true);
    });
  });
}

window.ESH = {
  API_BASE,
  PUBLIC_NAV,
  STAFF_NAV,
  el,
  esc,
  uid,
  fmtDate,
  fmtDateTime,
  statusClass,
  statusBadge,
  showToast,
  showLoading,
  showSkeleton,
  showEmpty,
  showError,
  setLoading,
  unsetLoading,
  getParam,
  openModal,
  closeModal,
  confirmModal,
};
