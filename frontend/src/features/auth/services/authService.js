const TOKEN_KEY = "procureai_token";

// auth-service attend { email, password } dans le corps (jamais dans l'URL) et renvoie { token }
export async function login(email, password) {
  let response;
  try {
    response = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });
  } catch {
    throw new Error("Impossible de joindre le serveur. Vérifiez votre connexion.");
  }

  if (response.status >= 502) {
    throw new Error("Le service est momentanément indisponible. Réessayez dans un instant.");
  }
  if (!response.ok) {
    throw new Error("Email ou mot de passe incorrect.");
  }

  const { token } = await response.json();
  localStorage.setItem(TOKEN_KEY, token);
  return getCurrentUser();
}

export function logout() {
  localStorage.removeItem(TOKEN_KEY);
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

// Lit l'utilisateur dans le token (id, email, role). Renvoie null si absent ou expire.
export function getCurrentUser() {
  const token = getToken();
  if (!token) return null;

  try {
    const payload = JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
    if (payload.exp * 1000 < Date.now()) {
      logout();
      return null;
    }
    return { id: payload.id, email: payload.sub, role: payload.role };
  } catch {
    logout();
    return null;
  }
}
