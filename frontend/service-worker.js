/* eSHCAT — service-worker.js
   Caches public resources for offline-friendly usage.

   IMPORTANT: We only cache public, non-sensitive resources.
   Staff pages and API responses are NOT cached.
 */

const CACHE_NAME = "eshcat-v2";
const ASSETS = [
  "/",
  "/index.html",
  "/manifest.json",
  "/css/style.css",
  "/css/components.css",
  "/css/responsive.css",
  "/js/utils.js",
  "/js/storage.js",
  "/js/api.js",
  "/js/app.js",
  "/js/services.js",
  "/js/applications.js",
  "/js/tracking.js",
  "/js/announcements.js",
  "/js/appointments.js",
  "/js/reports.js",
  "/js/offices.js",
  "/pages/services.html",
  "/pages/service-details.html",
  "/pages/apply.html",
  "/pages/track.html",
  "/pages/appointments.html",
  "/pages/reports.html",
  "/pages/announcements.html",
  "/pages/offices.html",
  "/assets/logo/favicon.svg",
];

self.addEventListener("install", (event) => {
  event.waitUntil(
    caches
      .open(CACHE_NAME)
      .then((cache) => cache.addAll(ASSETS))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches
      .keys()
      .then((keys) => Promise.all(keys.filter((k) => k !== CACHE_NAME).map((k) => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener("fetch", (event) => {
  const url = new URL(event.request.url);

  // Never intercept API calls or staff pages.
  if (url.pathname.startsWith("/api/") || url.pathname.includes("/staff/")) {
    return;
  }

  // Navigations: network-first, fall back to cache.
  if (event.request.mode === "navigate") {
    event.respondWith(
      fetch(event.request)
        .then((response) => {
          const copy = response.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put(event.request, copy));
          return response;
        })
        .catch(() => caches.match(event.request).then((hit) => hit || caches.match("/index.html")))
    );
    return;
  }

  // Static assets: cache-first with network fallback.
  event.respondWith(
    caches.match(event.request).then(
      (cached) =>
        cached ||
        fetch(event.request).then((response) => {
          if (response.ok && url.origin === self.location.origin) {
            const copy = response.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put(event.request, copy));
          }
          return response;
        })
    )
  );
});