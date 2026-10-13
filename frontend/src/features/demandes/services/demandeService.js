import { apiDelete, apiGet, apiPost, apiPut } from "../../../api/client";

// MANAGER et ADMIN voient toutes les demandes, les autres seulement les leurs
export function getDemandes(role) {
  return apiGet(role === "MANAGER" || role === "ADMIN" ? "/api/demandes" : "/api/demandes/mine");
}

export function createDemande(demande) {
  return apiPost("/api/demandes", demande);
}

export function approveDemande(id) {
  return apiPut(`/api/demandes/${id}/approve`);
}

export function rejectDemande(id) {
  return apiPut(`/api/demandes/${id}/reject`);
}

export function deleteDemande(id) {
  return apiDelete(`/api/demandes/${id}`);
}
