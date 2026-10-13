import { apiDelete, apiGet, apiPost, apiPut } from "../../../api/client";

// Roles que l'utilisateur connecte peut gerer (meme regle que auth-service)
export function manageableRoles(role) {
  if (role === "ADMIN") return ["MANAGER", "COMPTABLE", "MAGASINIER", "EMPLOYEE"];
  if (role === "MANAGER") return ["EMPLOYEE"];
  return [];
}

// ADMIN recoit tous les comptes sauf les admins, MANAGER seulement les employes
export function getUsers() {
  return apiGet("/api/users");
}

export function createUser(user) {
  return apiPost("/api/users", user);
}

// Mot de passe vide = le backend garde l'ancien
export function updateUser(id, user) {
  return apiPut(`/api/users/${id}`, user);
}

export function deleteUser(id) {
  return apiDelete(`/api/users/${id}`);
}
