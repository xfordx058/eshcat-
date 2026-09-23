(function () {
  const ESH = window.ESH;
  const root = document.getElementById("comelecPublic");
  const esc = (value) => ESH.esc(value == null ? "" : value);

  function draw(settings) {
    const intake = settings.active
      ? `<section class="card comelec-public-card"><span class="comelec-kicker">Queue intake is open</span><h2>Get a present number</h2><p>${esc(settings.announcement || "Complete the form to join today’s voter verification queue.")}</p><div class="comelec-room-pill">Assigned room: <strong>${esc(settings.room)}</strong></div><form id="queueForm"><label>Full name<input id="queueName" required placeholder="Juan Dela Cruz"></label><label>Precinct number<input id="queuePrecinct" required placeholder="e.g. 001A"></label><label>Email address <span class="note">optional</span><input id="queueEmail" type="email" placeholder="you@example.com"></label><label>Purpose<select id="queuePurpose"><option>Voter verification</option><option>Voter certification follow-up</option><option>Election-day assistance</option></select></label><button class="btn btn-primary" type="submit">Get Present Number</button></form><div id="queueResult"></div></section>`
      : `<section class="card comelec-public-card"><div class="comelec-icon">◷</div><h2>Election-day queue is not active</h2><p>${esc(settings.announcement || "Please check back when COMELEC enables the queue.")}</p><a class="btn btn-secondary" href="services.html">View Voter Certification</a></section>`;
    const lookup = settings.lookup_enabled
      ? `<section class="card comelec-public-card"><span class="comelec-kicker">Already have a number?</span><h2>Search present number</h2><p>Enter your present number to see the current queue status and assigned room.</p><form id="lookupForm" class="comelec-lookup"><label>Present number<input id="lookupNumber" type="number" min="1" required placeholder="e.g. 24"></label><button class="btn btn-secondary" type="submit">Search Number</button></form><div id="lookupResult"></div><p class="note">Search is enabled by COMELEC for today’s queue.</p></section>`
      : `<section class="card comelec-public-card"><span class="comelec-kicker">Present-number search</span><h2>Search is currently unavailable</h2><p>COMELEC will enable this search when today’s queue is ready.</p></section>`;
    root.innerHTML = `<div class="comelec-public-grid">${intake}${lookup}</div>`;

    document.getElementById("queueForm")?.addEventListener("submit", async (event) => {
      event.preventDefault();
      const result = document.getElementById("queueResult");
      ESH.showLoading(result, "Issuing your present number...");
      try {
        const data = await ESH.api.post("/comelec/queue", { full_name: document.getElementById("queueName").value.trim(), precinct_number: document.getElementById("queuePrecinct").value.trim(), email: document.getElementById("queueEmail").value.trim(), purpose: document.getElementById("queuePurpose").value });
        result.innerHTML = `<div class="comelec-ticket"><small>Your present number</small><strong>${data.present_number}</strong><span>${esc(data.room)} · ${esc(data.status)}</span><p>Keep this number and proceed to the assigned COMELEC room.</p></div>`;
        document.getElementById("queueForm").reset();
      } catch (err) { ESH.showEmpty(result, err.message); }
    });
    document.getElementById("lookupForm")?.addEventListener("submit", async (event) => {
      event.preventDefault();
      const result = document.getElementById("lookupResult");
      ESH.showLoading(result, "Searching...");
      try {
        const data = await ESH.api.get(`/comelec/queue/${encodeURIComponent(document.getElementById("lookupNumber").value)}`);
        result.innerHTML = `<div class="comelec-lookup-result"><strong>Present No. ${data.present_number}</strong><span>${esc(data.status)}</span><small>${esc(data.room)} · Precinct ${esc(data.precinct_number)}</small></div>`;
      } catch (err) { ESH.showEmpty(result, err.message); }
    });
  }

  document.addEventListener("DOMContentLoaded", async () => {
    try { draw(await ESH.api.get("/comelec/settings")); } catch (err) { ESH.showEmpty(root, err.message); }
  });
})();
