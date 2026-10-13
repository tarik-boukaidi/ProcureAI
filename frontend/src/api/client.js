import { getToken, logout } from "../features/auth/services/authService";

// Appel vers le gateway avec le token. Si le token n'est plus valide : retour au login.
// body : un objet (envoye en JSON) ou un FormData (envoi de fichier).
async function request(method, path, body) {
  const headers = { Authorization: `Bearer ${getToken()}` };
  const isFile = body instanceof FormData;
  // Pour un FormData, le navigateur met lui-meme le Content-Type (avec le "boundary")
  if (body && !isFile) headers["Content-Type"] = "application/json";

  const response = await fetch(path, {
    method,
    headers,
    body: isFile ? body : body ? JSON.stringify(body) : undefined,
  });

  if (response.status === 401) {
    logout();
    window.location.href = "/login";
    throw new Error("Session expirée");
  }
  if (!response.ok) {
    const error = new Error(`Erreur ${response.status} sur ${path}`);
    error.status = response.status;
    throw error;
  }
  // 204 No Content (ex : DELETE) : pas de corps a lire
  return response.status === 204 ? null : response.json();
}

export const apiGet = (path) => request("GET", path);
export const apiPost = (path, body) => request("POST", path, body);
export const apiPut = (path, body) => request("PUT", path, body);
export const apiDelete = (path) => request("DELETE", path);
