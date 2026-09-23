/* eSHCAT — storage.js
   LocalStorage drafts + IndexedDB offline queue.

   IMPORTANT: the frontend must distinguish "Saved Locally" from
   "Accepted by Server". A reference number only exists after the
   backend confirms submission.
 */

const DRAFTS_KEY = "eshcat.drafts";

function isOnline() {
  return navigator.onLine;
}

function onNetworkChange(handler) {
  window.addEventListener("online", handler);
  window.addEventListener("offline", handler);
}

/* ---------- Drafts (LocalStorage) ---------- */

function listDrafts() {
  try {
    return JSON.parse(localStorage.getItem(DRAFTS_KEY) || "[]");
  } catch {
    return [];
  }
}

function saveDraft(draft) {
  const drafts = listDrafts();
  drafts.push({ ...draft, localId: draft.localId || `draft-${Date.now().toString(36)}`, savedAt: new Date().toISOString() });
  localStorage.setItem(DRAFTS_KEY, JSON.stringify(drafts));
  return drafts[drafts.length - 1];
}

function updateDraft(localId, patch) {
  const drafts = listDrafts();
  const idx = drafts.findIndex((d) => d.localId === localId);
  if (idx === -1) return null;
  drafts[idx] = { ...drafts[idx], ...patch, savedAt: new Date().toISOString() };
  localStorage.setItem(DRAFTS_KEY, JSON.stringify(drafts));
  return drafts[idx];
}

function clearDraft(localId) {
  localStorage.setItem(
    DRAFTS_KEY,
    JSON.stringify(listDrafts().filter((d) => d.localId !== localId))
  );
}

/* ---------- IndexedDB offline queue ---------- */

const DB_NAME = "eshcat-queue";
const STORE = "submissions";

function openQueue() {
  return new Promise((resolve, reject) => {
    if (!("indexedDB" in window)) {
      reject(new Error("IndexedDB not supported."));
      return;
    }
    const req = indexedDB.open(DB_NAME, 1);
    req.onupgradeneeded = () => {
      req.result.createObjectStore(STORE, { keyPath: "id" });
    };
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
}

async function enqueueSubmission(payload) {
  const db = await openQueue();
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE, "readwrite");
    tx.objectStore(STORE).add({ id: `q-${Date.now().toString(36)}`, payload, createdAt: new Date().toISOString() });
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
}

async function pendingSubmissions() {
  const db = await openQueue();
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE, "readonly");
    const req = tx.objectStore(STORE).getAll();
    req.onsuccess = () => resolve(req.result || []);
    req.onerror = () => reject(req.error);
  });
}

async function removeSubmission(id) {
  const db = await openQueue();
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE, "readwrite");
    tx.objectStore(STORE).delete(id);
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
}

async function syncPending() {
  const pending = await pendingSubmissions();
  let synced = 0;
  for (const item of pending) {
    try {
      const data = await window.ESH.api.post(item.payload.endpoint, item.payload.data);
      await removeSubmission(item.id);
      synced += 1;
      if (item.payload.onSynced) item.payload.onSynced(data);
    } catch {
      /* keep in queue for next retry */
    }
  }
  return synced;
}

window.ESH.storage = {
  isOnline,
  onNetworkChange,
  listDrafts,
  saveDraft,
  updateDraft,
  clearDraft,
  enqueueSubmission,
  pendingSubmissions,
  removeSubmission,
  syncPending,
};