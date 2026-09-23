(function () {
  const api = window.ESH.api;
  const $ = (selector) => document.querySelector(selector);
  const esc = (value) => String(value == null ? "" : value).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
  let incidents = [];
  let incidentsSignature = "";
  let staffLocation = null;
  let audioContext = null;
  let sirenOscillator = null;
  let sirenLfo = null;
  let sirenGain = null;
  let sirenModulation = null;
  let alarmEnabled = false;

  function startSiren() {
    if (!audioContext || audioContext.state !== "running" || sirenOscillator) return;
    sirenOscillator = audioContext.createOscillator();
    sirenGain = audioContext.createGain();
    sirenLfo = audioContext.createOscillator();
    sirenModulation = audioContext.createGain();

    sirenOscillator.type = "sine";
    sirenOscillator.frequency.value = 900;
    sirenGain.gain.setValueAtTime(0.0001, audioContext.currentTime);
    sirenGain.gain.exponentialRampToValueAtTime(0.16, audioContext.currentTime + 0.12);

    // A slow pitch sweep gives the alarm a familiar ambulance siren rise and fall.
    sirenLfo.type = "sine";
    sirenLfo.frequency.value = 0.45;
    sirenModulation.gain.value = 270;
    sirenLfo.connect(sirenModulation);
    sirenModulation.connect(sirenOscillator.frequency);
    sirenOscillator.connect(sirenGain);
    sirenGain.connect(audioContext.destination);
    sirenOscillator.start();
    sirenLfo.start();
  }

  function stopSiren() {
    if (!audioContext || !sirenOscillator) return;
    const stopAt = audioContext.currentTime + 0.18;
    sirenGain.gain.cancelScheduledValues(audioContext.currentTime);
    sirenGain.gain.setTargetAtTime(0.0001, audioContext.currentTime, 0.035);
    sirenOscillator.stop(stopAt);
    sirenLfo.stop(stopAt);
    sirenOscillator = null;
    sirenLfo = null;
    sirenGain = null;
    sirenModulation = null;
  }

  function syncAlarm() {
    const hasNewAlert = incidents.some((incident) => incident.status === "Open" && !incident.ignored_by_me);
    document.body.classList.toggle("drrmo-alarming", hasNewAlert);
    if (hasNewAlert && alarmEnabled) startSiren();
    else stopSiren();
  }

  function mapLinks(incident) {
    const query = incident.latitude != null && incident.longitude != null
      ? `${incident.latitude},${incident.longitude}`
      : `${incident.location || "Catarman, Northern Samar"}, Catarman, Northern Samar`;
    const encoded = encodeURIComponent(query);
    const origin = staffLocation ? `${staffLocation.latitude},${staffLocation.longitude}` : "";
    const encodedOrigin = encodeURIComponent(origin);
    return {
      embed: staffLocation
        ? `https://maps.google.com/maps?saddr=${encodedOrigin}&daddr=${encoded}&output=embed`
        : `https://maps.google.com/maps?q=${encoded}&z=16&output=embed`,
      directions: `https://www.google.com/maps/dir/?api=1&${staffLocation ? `origin=${encodedOrigin}&` : ""}destination=${encoded}&travelmode=driving`,
    };
  }

  function render() {
    const visibleIncidents = incidents.filter((incident) => !incident.ignored_by_me);
    const openCount = visibleIncidents.filter((incident) => incident.status === "Open").length;
    const respondingCount = visibleIncidents.filter((incident) => incident.status === "Responding").length;
    const redCount = visibleIncidents.filter((incident) => incident.severity === "Red").length;
    $("#openIncidentCount").textContent = openCount;
    $("#respondingIncidentCount").textContent = respondingCount;
    $("#redIncidentCount").textContent = redCount;
    $("#lastRefreshTime").textContent = new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    const board = $("#incidentBoard");
    if (!visibleIncidents.length) {
      board.innerHTML = '<div class="dp-empty">No active incidents. New public alerts will appear here.</div>';
      syncAlarm();
      return;
    }
    board.innerHTML = visibleIncidents.map((incident) => {
      const level = esc(incident.severity.toLowerCase());
      const maps = mapLinks(incident);
      const coordinates = incident.latitude != null && incident.longitude != null
        ? `${Number(incident.latitude).toFixed(5)}, ${Number(incident.longitude).toFixed(5)}`
        : "Location entered by reporter";
      const gpsAccuracy = incident.accuracy_meters != null
        ? `±${Math.max(1, Math.round(Number(incident.accuracy_meters)))} m (device estimate)`
        : incident.latitude != null ? "Not reported by device" : "GPS unavailable; using approximate location";
      const originSummary = staffLocation
        ? `Responder GPS ${Number(staffLocation.latitude).toFixed(5)}, ${Number(staffLocation.longitude).toFixed(5)} (±${Math.max(1, Math.round(staffLocation.accuracy))} m)`
        : "Responder origin not shared yet";
      const action = incident.status === "Open"
        ? `<button class="dp-btn primary drrmo-take" data-id="${Number(incident.id)}" type="button">Take response and silence alarm</button><button class="dp-btn drrmo-ignore" data-id="${Number(incident.id)}" type="button">Ignore for me</button>`
        : `<button class="dp-btn drrmo-resolve" data-id="${Number(incident.id)}" type="button">Mark resolved</button>`;
      return `<article class="drrmo-incident ${level}">
        <div class="drrmo-incident-header"><div><span class="emergency-level ${level}">${esc(incident.severity)} level</span><h3>${esc(incident.category)}</h3><p class="drrmo-reference">${esc(incident.reference_number)} · ${esc(incident.created_at)}</p></div><span class="drrmo-status ${incident.status === "Open" ? "open" : "responding"}">${esc(incident.status)}</span></div>
        <div class="drrmo-incident-main"><div class="drrmo-incident-info"><p><strong>Reported location:</strong> ${esc(incident.location || "Device coordinates")}</p><p><strong>Coordinates:</strong> ${esc(coordinates)}</p><p><strong>GPS accuracy:</strong> ${esc(gpsAccuracy)}</p><p><strong>Details:</strong> ${esc(incident.description || "No additional details provided.")}</p><p><strong>Responder:</strong> ${esc(incident.responder_name || "Not taken yet")}</p>
          <p class="drrmo-route-summary"><strong>Responder location:</strong> ${esc(originSummary)}<br><strong>Rescue location:</strong> ${esc(coordinates)}<br><small>Google Maps shows the suggested driving route and calculates the ETA.</small></p>
          <div class="drrmo-actions">${action}<label class="drrmo-level-control">Incident level<select data-level-for="${Number(incident.id)}"><option ${incident.severity === "Yellow" ? "selected" : ""}>Yellow</option><option ${incident.severity === "Orange" ? "selected" : ""}>Orange</option><option ${incident.severity === "Red" ? "selected" : ""}>Red</option></select></label><button class="dp-btn drrmo-update-level" data-id="${Number(incident.id)}" type="button">Update level</button><a class="dp-btn" href="${maps.directions}" target="_blank" rel="noopener noreferrer">Route &amp; ETA in Google Maps</a></div>
        </div><iframe class="drrmo-map" title="Map for ${esc(incident.reference_number)}" src="${maps.embed}" loading="lazy" referrerpolicy="no-referrer-when-downgrade"></iframe></div>
      </article>`;
    }).join("");
    board.querySelectorAll(".drrmo-take").forEach((button) => button.addEventListener("click", () => updateIncident(button, "respond")));
    board.querySelectorAll(".drrmo-resolve").forEach((button) => button.addEventListener("click", () => updateIncident(button, "resolve")));
    board.querySelectorAll(".drrmo-ignore").forEach((button) => button.addEventListener("click", () => updateIncident(button, "ignore")));
    board.querySelectorAll(".drrmo-update-level").forEach((button) => button.addEventListener("click", () => updateIncident(button, "set_severity")));
    syncAlarm();
  }

  async function updateIncident(button, action) {
    button.disabled = true;
    try {
      const payload = { action };
      if (action === "set_severity") payload.severity = $(`[data-level-for="${button.dataset.id}"]`).value;
      const result = await api.patch(`/staff/emergencies/${button.dataset.id}`, payload);
      if (action === "respond") {
        const assigned = incidents.find((incident) => String(incident.id) === button.dataset.id);
        if (assigned) {
          assigned.status = result.status;
          assigned.responder_name = result.responder_name;
        }
      }
      await refreshIncidents();
      const notice = $("#drrmoWarning");
      notice.textContent = action === "resolve"
        ? `Incident resolved by ${result.resolved_by_name || "MDRRMO staff"}. The responder and resolution are recorded.`
        : action === "respond"
          ? `Response taken by ${result.responder_name || "MDRRMO staff"}. Alarm silenced for this incident.`
          : action === "ignore"
            ? "Incident ignored for your account and added to your response history. It remains visible to other MDRRMO staff."
            : `Incident level updated to ${result.severity}.`;
      notice.classList.add("drrmo-action-notice");
    } catch (error) {
      button.disabled = false;
      window.alert(error.message);
    }
  }

  async function refreshIncidents() {
    try {
      const nextIncidents = await api.get("/staff/emergencies");
      $("#connectionState").textContent = "Connected · polling every 4 seconds";
      $("#connectionState").classList.add("connected");
      const nextSignature = JSON.stringify(nextIncidents);
      if (nextSignature !== incidentsSignature) {
        incidents = nextIncidents;
        incidentsSignature = nextSignature;
        render();
      } else {
        $("#lastRefreshTime").textContent = new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
      }
    } catch (error) {
      $("#connectionState").textContent = error.status === 403 ? "DRRMO access required" : "Connection lost · retrying";
      $("#connectionState").classList.remove("connected");
    }
  }

  async function refreshHistory() {
    const container = $("#responseHistory");
    container.innerHTML = '<div class="dp-loading">Loading your history...</div>';
    try {
      const rows = await api.get("/staff/emergencies/history");
      if (!rows.length) {
        container.innerHTML = '<div class="dp-empty">No response actions recorded for your account yet.</div>';
        return;
      }
      container.innerHTML = `<div class="dp-table-wrap"><table class="dp-table"><thead><tr><th>Incident</th><th>Level</th><th>Your action</th><th>Incident status</th><th>When</th><th>Responder</th></tr></thead><tbody>${rows.map((row) => `<tr><td><strong>${esc(row.category)}</strong><small class="drrmo-history-ref">${esc(row.reference_number)}</small></td><td><span class="emergency-level ${esc(row.severity.toLowerCase())}">${esc(row.severity)}</span></td><td><span class="drrmo-history-action ${esc(row.staff_action.toLowerCase())}">${esc(row.staff_action)}</span></td><td>${esc(row.status)}</td><td>${esc(row.action_at)}</td><td>${esc(row.responder_name || row.resolved_by_name || "—")}</td></tr>`).join("")}</tbody></table></div>`;
    } catch (error) {
      container.innerHTML = `<div class="dp-empty">${esc(error.message)}</div>`;
    }
  }

  function showView(name) {
    document.querySelectorAll("[data-drrmo-view]").forEach((view) => { view.hidden = view.dataset.drrmoView !== name; });
    document.querySelectorAll("[data-drrmo-view-link]").forEach((link) => link.classList.toggle("active", link.dataset.drrmoViewLink === name));
    if (name === "history") refreshHistory();
  }

  async function init() {
    const staff = await api.get("/staff/me");
    if (!String(staff.department_name || "").toLowerCase().includes("disaster risk reduction")) {
      $("#incidentBoard").innerHTML = '<div class="dp-empty">This emergency portal is restricted to MDRRMO staff.</div>';
      $("#connectionState").textContent = "Access denied";
      return;
    }
    $("#drrmoStaffName").textContent = staff.name;
    $("#drrmoStaffRole").textContent = staff.role;
    $("#drrmoProfile").innerHTML = `<div class="drrmo-profile-card"><div class="drrmo-profile-avatar">${esc((staff.name || "R").split(/\s+/).map((part) => part[0]).slice(0, 2).join("").toUpperCase())}</div><div><h3>${esc(staff.name)}</h3><p>${esc(staff.role)}</p></div></div><dl><dt>Email</dt><dd>${esc(staff.email || "Not provided")}</dd><dt>Department</dt><dd>${esc(staff.department_name)}</dd><dt>Account access</dt><dd>DRRMO incident response</dd></dl>`;
    document.querySelectorAll("[data-drrmo-view-link]").forEach((link) => link.addEventListener("click", () => showView(link.dataset.drrmoViewLink)));
    $("#refreshHistory").addEventListener("click", refreshHistory);
    $("#shareStaffLocation").addEventListener("click", shareStaffLocation);
    $("#enableAlarm").addEventListener("click", async () => {
      try {
        const Context = window.AudioContext || window.webkitAudioContext;
        if (!Context) throw new Error("Alarm sound is not supported by this browser.");
        audioContext = audioContext || new Context();
        await audioContext.resume();
        alarmEnabled = true;
        $("#enableAlarm").textContent = "Alarm sound enabled";
        $("#enableAlarm").disabled = true;
        syncAlarm();
      } catch (error) {
        window.alert(error.message);
      }
    });
    $("#refreshIncidents").addEventListener("click", refreshIncidents);
    await refreshIncidents();
    window.setInterval(refreshIncidents, 4000);
  }

  function shareStaffLocation() {
    const button = $("#shareStaffLocation");
    const status = $("#staffLocationStatus");
    if (!navigator.geolocation) {
      status.textContent = "This browser does not provide location access.";
      return;
    }
    button.disabled = true;
    status.textContent = "Getting your current GPS location...";
    navigator.geolocation.getCurrentPosition((position) => {
      staffLocation = {
        latitude: position.coords.latitude,
        longitude: position.coords.longitude,
        accuracy: position.coords.accuracy,
      };
      status.textContent = `Location shared for this session (±${Math.max(1, Math.round(staffLocation.accuracy))} m). Not saved.`;
      render();
      button.disabled = false;
      button.textContent = "Update my location";
    }, () => {
      status.textContent = "Could not get GPS. Allow location access and use a trusted HTTPS address.";
      button.disabled = false;
    }, { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 });
  }

  document.addEventListener("DOMContentLoaded", () => init().catch((error) => {
    $("#connectionState").textContent = error.message;
    $("#incidentBoard").innerHTML = `<div class="dp-empty">${esc(error.message)}</div>`;
  }));
})();
