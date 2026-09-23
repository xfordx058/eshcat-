/* eSHCAT — app.js
   Shared chrome: header, footer, navigation, offline banner,
   PWA install prompt.
 */

(function () {
  const ESH = window.ESH;

  function currentPath() {
    return window.location.pathname;
  }

  function isActive(href) {
    const path = currentPath();
    if (href.endsWith("index.html") || href.endsWith("/")) {
      return path === "/" || path.endsWith("index.html");
    }
    return path.endsWith(href);
  }

  function isStaffPage() {
    return currentPath().includes("/staff/");
  }

  function renderNavigation() {
    return ESH.PUBLIC_NAV.map(
      (item) => `<a href="${item.href}"${isActive(item.href) ? ' class="active"' : ""}>${item.label}</a>`
    ).join("");
  }

  function injectHeader() {
    const header = document.createElement("header");
    header.className = "site-header";
    const staffMode = isStaffPage();
    if (staffMode) header.classList.add("staff-mode");
    header.innerHTML = `
      <div class="header-inner">
        <a href="/" class="brand" aria-label="eSHCAT home">
          <span class="brand-mark" aria-hidden="true">eS</span>
          <span>eSHCAT<small>Electronic Services Hub for Catarman</small></span>
        </a>
        <nav class="main-nav" aria-label="Primary navigation">
          ${staffMode ? `
            <a href="/pages/staff/dashboard.html">Dashboard</a>
            <a href="/pages/staff/applications.html">Applications</a>
          ` : renderNavigation()}
          <a href="${staffMode ? "/pages/staff/login.html" : "/pages/staff/login.html"}" class="staff-link">${staffMode ? "Exit" : "Staff Login"}</a>
        </nav>
        <button class="nav-toggle" aria-label="Toggle navigation menu" aria-expanded="false">☰</button>
      </div>
    `;
    document.body.prepend(header);

    const toggle = header.querySelector(".nav-toggle");
    const nav = header.querySelector(".main-nav");
    toggle.addEventListener("click", () => {
      const open = nav.classList.toggle("open");
      toggle.setAttribute("aria-expanded", String(open));
    });
  }

  function injectFooter() {
    const footer = document.createElement("footer");
    footer.className = "site-footer";
    footer.innerHTML = `
      <div class="footer-inner">
        <div>
          <h4>eSHCAT</h4>
          <p class="note">Electronic Services Hub for Catarman</p>
          <p class="note">One Municipality. Connected Services. Easier Access.</p>
        </div>
        <div>
          <h4>Quick Links</h4>
          <ul>
            <li><a href="/pages/services.html">Municipal Services</a></li>
            <li><a href="/pages/track.html">Track Request</a></li>
            <li><a href="/pages/announcements.html">Announcements</a></li>
            <li><a href="/pages/offices.html">Offices</a></li>
          </ul>
        </div>
        <div>
          <h4>Municipal Hall</h4>
          <ul>
            <li>Catarman, Northern Samar</li>
            <li>Mon–Fri · 8:00 AM – 5:00 PM</li>
            <li><a href="/pages/reports.html">Report a Concern</a></li>
          </ul>
        </div>
      </div>
      <div class="footer-bottom">
        Hackathon prototype by <strong>Walang Kanin Bossing</strong>. Not an official LGU service.
      </div>
    `;
    document.body.appendChild(footer);
  }

  function injectNetworkBanner() {
    const banner = document.createElement("div");
    banner.className = "network-banner";
    banner.id = "networkBanner";

    function update() {
      const offline = typeof navigator !== "undefined" && !navigator.onLine;
      banner.classList.toggle("offline", offline);
      if (offline) {
        banner.textContent = "● Offline — Your connection is unavailable. Drafts will sync when you're back online.";
      }
    }

    update();
    ESH.storage.onNetworkChange(update);
    document.body.prepend(banner);
  }

  function setupPWAInstall() {
    let deferredPrompt = null;
    window.addEventListener("beforeinstallprompt", (e) => {
      e.preventDefault();
      deferredPrompt = e;
      const banner = document.getElementById("installBanner");
      if (banner) banner.classList.add("show");
    });

    const banner = document.getElementById("installBanner");
    if (banner) {
      banner.querySelector("[data-install]")?.addEventListener("click", async () => {
        if (!deferredPrompt) return;
        deferredPrompt.prompt();
        await deferredPrompt.userChoice;
        deferredPrompt = null;
        banner.classList.remove("show");
      });
      banner.querySelector("[data-dismiss-install]")?.addEventListener("click", () => {
        banner.classList.remove("show");
      });
    }
  }

  function registerServiceWorker() {
    if (!("serviceWorker" in navigator)) return;
    if (window.location.protocol !== "https:" && window.location.hostname !== "localhost" && window.location.hostname !== "127.0.0.1") return;
    window.addEventListener("load", () => {
      navigator.serviceWorker.register("/service-worker.js").catch(() => {});
    });
  }

  document.addEventListener("DOMContentLoaded", () => {
    if (document.body) {
      injectNetworkBanner();
      injectHeader();
      injectFooter();
      setupPWAInstall();
      registerServiceWorker();
    }
  });
})();