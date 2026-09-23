/* eSHCAT — utils.js
   Shared helpers: DOM, toasts, formatting, status classes. */

const API_BASE = "/api";

const PUBLIC_NAV = [
  { href: "/", label: "Home" },
  { href: "/pages/services.html", label: "Services" },
  { href: "/pages/track.html", label: "Track Request" },
  { href: "/pages/appointments.html", label: "Appointments" },
  { href: "/pages/reports.html", label: "Report Concern" },
  { href: "/pages/announcements.html", label: "Announcements" },
  { href: "/pages/offices.html", label: "Offices" },
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

function showEmpty(container, message, actionHtml = "") {
  const box = el("div", { class: "empty" }, [
    el("div", { class: "big", text: "🕊️" }),
    el("p", { text: message }),
  ]);
  if (actionHtml) box.insertAdjacentHTML("beforeend", actionHtml);
  container.innerHTML = "";
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

window.ESH = {
  API_BASE,
  PUBLIC_NAV,
  el,
  esc,
  uid,
  fmtDate,
  fmtDateTime,
  statusClass,
  statusBadge,
  showToast,
  showLoading,
  showEmpty,
  setLoading,
  unsetLoading,
  getParam,
};