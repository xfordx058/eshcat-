/* eSHCAT — api.js
   Centralized API communication. All fetch calls go through here. */

const { API_BASE } = window.ESH;

async function request(path, options = {}) {
  const config = {
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    ...options,
  };
  if (config.body && typeof config.body !== "string") {
    config.body = JSON.stringify(config.body);
  }

  let response;
  try {
    response = await fetch(`${API_BASE}${path}`, config);
  } catch {
    const error = new Error("Unable to connect to the server.");
    error.network = true;
    throw error;
  }

  let data = null;
  try {
    data = await response.json();
  } catch {
    /* no JSON body */
  }

  if (!response.ok) {
    const message = data && data.error ? data.error : `Request failed (${response.status}).`;
    const error = new Error(message);
    error.status = response.status;
    error.data = data;
    throw error;
  }
  return data;
}

window.ESH.api = {
  get: (path) => request(path),
  post: (path, body) => request(path, { method: "POST", body }),
  patch: (path, body) => request(path, { method: "PATCH", body }),
};