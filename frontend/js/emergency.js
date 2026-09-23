/* eSHCAT — emergency.js
   Public "Emergency & Important Numbers" card on the homepage.
   Renders the DB-backed list; keeps the static markup as an offline fallback. */

(function () {
  function renderHome() {
    const container = document.getElementById("emergencyContacts");
    if (!container || !window.ESH.api) return;
    window.ESH.api
      .get("/emergency-numbers")
      .then((rows) => {
        if (!rows || !rows.length) return;
        container.innerHTML = "";
        rows.forEach((r) => {
          const item = document.createElement("div");
          item.className = "contact-item";
          const k = document.createElement("div");
          k.className = "k";
          k.textContent = r.label;
          const v = document.createElement("div");
          v.className = "v";
          v.textContent = r.value;
          item.appendChild(k);
          item.appendChild(v);
          container.appendChild(item);
        });
      })
      .catch(() => {
        /* keep static fallback content */
      });
  }

  function requestDeviceLocation() {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) return reject(new Error("This device does not provide location sharing."));
      navigator.geolocation.getCurrentPosition(
        (position) => resolve({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
          accuracy_meters: position.coords.accuracy,
        }),
        () => reject(new Error("Location sharing was unavailable.")),
        { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 }
      );
    });
  }

  function initFollowupDialog() {
    const dialog = document.getElementById("sosFollowupDialog");
    const form = document.getElementById("sosFollowupForm");
    const message = document.getElementById("sosFollowupMessage");
    if (!dialog || !form) return;
    let pendingFollowup = null;
    document.getElementById("sosSkip").addEventListener("click", () => {
      pendingFollowup = null;
      dialog.close();
    });
    form.addEventListener("submit", async (event) => {
      event.preventDefault();
      const category = document.getElementById("sosCategory").value;
      const location = document.getElementById("sosLocation").value.trim();
      const description = document.getElementById("sosDescription").value.trim();
      if (!category && !location && !description) {
        message.textContent = "Add information first, or choose Skip.";
        return;
      }
      if (!pendingFollowup) {
        dialog.close();
        return;
      }
      const saveButton = document.getElementById("sosSaveDetails");
      saveButton.disabled = true;
      message.textContent = "Sending additional information...";
      try {
        await window.ESH.api.patch(`/emergencies/${pendingFollowup.id}/details`, {
          followup_token: pendingFollowup.token,
          category,
          location,
          description,
        });
        pendingFollowup = null;
        dialog.close();
        form.reset();
      } catch (error) {
        message.textContent = error.message;
      } finally {
        saveButton.disabled = false;
      }
    });
    return {
      show(result) {
        pendingFollowup = { id: result.id, token: result.followup_token };
        document.getElementById("sosReference").textContent = `Reference ${result.reference_number} · MDRRMO has been alerted.`;
        message.textContent = "";
        dialog.showModal();
      },
    };
  }

  function initEmergencyAlert() {
    const button = document.getElementById("emergencySend");
    const followup = initFollowupDialog();
    if (!button || !window.ESH.api) return;
    button.addEventListener("click", async () => {
      button.disabled = true;
      button.setAttribute("aria-label", "Sending SOS emergency alert");
      button.innerHTML = "<span>...</span><small>Sending</small>";
      const locationPromise = requestDeviceLocation().catch(() => null);
      try {
        const result = await window.ESH.api.post("/emergencies", {
          severity: "Red",
          category: "Other",
        });
        if (followup) followup.show(result);
        locationPromise.then((coordinates) => {
          const locationStatus = document.getElementById("sosLocationStatus");
          if (!coordinates) {
            locationStatus.textContent = "GPS unavailable. Add a nearby landmark if you can.";
            return;
          }
          locationStatus.textContent = `Device location shared (reported accuracy about ±${Math.max(1, Math.round(coordinates.accuracy_meters))} m).`;
          window.ESH.api.patch(`/emergencies/${result.id}/details`, {
            followup_token: result.followup_token,
            ...coordinates,
          }).catch(() => {});
        });
      } catch (error) {
        window.alert(`SOS could not be sent: ${error.message}. Call 911 or MDRRMO directly.`);
      } finally {
        button.disabled = false;
        button.innerHTML = "<span>SOS</span><small>Help</small>";
        button.setAttribute("aria-label", "Send SOS emergency alert");
      }
    });
  }

  document.addEventListener("DOMContentLoaded", () => {
    if (document.body.dataset.page === "home") {
      renderHome();
      initEmergencyAlert();
    }
  });
})();
