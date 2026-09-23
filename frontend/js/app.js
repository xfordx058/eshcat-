/* eSHCAT — app.js
   Shared chrome: header, footer, navigation, offline banner,
   connectivity indicator, PWA install prompt.
   Design system: Modern Minimalism + Soft Glassmorphism + Bento UI
 */

(function () {
  window.ESH = window.ESH || {};
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
    const staffMode = isStaffPage();
    const items = staffMode
      ? ESH.STAFF_NAV
      : ESH.PUBLIC_NAV;
    return items
      .map((item) => `<a href="${item.href}"${isActive(item.href) ? ' class="active"' : ""}>${item.label}</a>`)
      .join("");
  }

  function injectHeader() {
    const staffMode = isStaffPage();
    const header = document.createElement("header");
    header.className = "site-header";
    if (staffMode) header.classList.add("staff-mode");
    header.innerHTML = `
      <div class="header-inner">
        <a href="/" class="brand" aria-label="eSHCAT home">
          <span class="brand-mark" aria-hidden="true">eS</span>
          <span>eSHCAT<small>Electronic Services Hub for Catarman</small></span>
        </a>
        <nav class="main-nav" aria-label="Primary navigation">
          ${renderNavigation()}
          <a href="/pages/staff/login.html" class="staff-link">${staffMode ? "Exit" : "Staff Login"}</a>
        </nav>
        <div class="header-right">
          <span class="conn-dot" id="connDot" title="Online" aria-label="Connection status"></span>
          <button class="nav-toggle" aria-label="Toggle navigation menu" aria-expanded="false">☰</button>
        </div>
      </div>
    `;
    document.body.prepend(header);

    const toggle = header.querySelector(".nav-toggle");
    const nav = header.querySelector(".main-nav");
    toggle.addEventListener("click", () => {
      const open = nav.classList.toggle("open");
      toggle.setAttribute("aria-expanded", String(open));
      nav.querySelectorAll("a").forEach((link) => {
        link.addEventListener("click", () => {
          nav.classList.remove("open");
          toggle.setAttribute("aria-expanded", "false");
        });
      });
    });

    const dot = header.querySelector("#connDot");
    const setConn = () => {
      const online = typeof navigator !== "undefined" && navigator.onLine;
      dot.classList.toggle("offline", !online);
      dot.title = online ? "Online" : "Offline";
    };
    setConn();
    window.addEventListener("online", setConn);
    window.addEventListener("offline", setConn);
    if (ESH.storage && ESH.storage.onNetworkChange) {
      ESH.storage.onNetworkChange(() => setConn());
    }
  }

  function injectFooter() {
    const footer = document.createElement("footer");
    footer.className = "site-footer";
    footer.innerHTML = `
      <div class="footer-inner">
        <div class="footer-brand">
          <a href="/" class="brand" aria-label="eSHCAT home">
            <span class="brand-mark" aria-hidden="true">eS</span>
            <span>eSHCAT<small>Electronic Services Hub for Catarman</small></span>
          </a>
          <p>One Municipality. Connected Services. Easier Access.
             Built as a hackathon prototype to showcase citizen-focused digital services.</p>
        </div>
        <div>
          <h4>Services</h4>
          <ul>
            <li><a href="/pages/services.html">Browse Services</a></li>
            <li><a href="/pages/track.html">Track Request</a></li>
            <li><a href="/pages/appointments.html">Book Appointment</a></li>
            <li><a href="/pages/reports.html">Report a Concern</a></li>
          </ul>
        </div>
        <div>
          <h4>Information</h4>
          <ul>
            <li><a href="/pages/announcements.html">Announcements</a></li>
            <li><a href="/pages/offices.html">Municipal Offices</a></li>
            <li><a href="/pages/about.html">About eSHCAT</a></li>
          </ul>
        </div>
        <div>
          <h4>Contact</h4>
          <ul>
            <li>Catarman Municipal Hall</li>
            <li>Catarman, Northern Samar, Philippines</li>
            <li>Mon–Fri · 8:00 AM – 5:00 PM</li>
            <li><a href="/pages/offices.html">View Office Directory</a></li>
          </ul>
        </div>
      </div>
      <div class="footer-bottom">
        © 2026 eSHCAT · Hackathon Prototype · Catarman, Northern Samar
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
    if (ESH.storage && ESH.storage.onNetworkChange) {
      ESH.storage.onNetworkChange(update);
    }
    document.body.prepend(banner);
  }

  function injectApiBanner() {
    const banner = document.createElement("div");
    banner.className = "network-banner api-banner";
    banner.id = "apiBanner";
    banner.textContent = "Cannot reach the eSHCAT API. Run the app through Flask: python -m backend.app";

    if (ESH.api && ESH.api.get) {
      ESH.api
        .get("/services")
        .catch(() => {
          document.body.prepend(banner);
        });
    }
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
    if (!document.body) return;
    injectNetworkBanner();
    injectApiBanner();
    if (!isStaffPage()) {
      injectHeader();
      injectFooter();
    }
    setupPWAInstall();
    registerServiceWorker();
  });
})();