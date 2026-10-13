import { apiGet, apiPut } from "../../../api/client";

// La cloche de la topbar ecoute cet evenement pour mettre a jour le nombre de non lues
export const NOTIFICATIONS_CHANGED = "notifications-changed";

export function getMyNotifications() {
  return apiGet("/api/notifications/mine");
}

export async function markAsRead(ids) {
  await Promise.all(ids.map((id) => apiPut(`/api/notifications/${id}/read`)));
  window.dispatchEvent(new Event(NOTIFICATIONS_CHANGED));
}
