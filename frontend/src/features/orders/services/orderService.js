import { apiGet, apiPost, apiPut } from "../../../api/client";
import { getSupplierNames } from "../../suppliers/services/supplierService";

// Commandes avec le nom du fournisseur (order-service ne renvoie que supplierId)
export async function loadOrders() {
  const [orders, supplierNames] = await Promise.all([apiGet("/api/orders"), getSupplierNames()]);
  return orders.map((order) => ({
    ...order,
    supplierName: supplierNames[order.supplierId] ?? `Fournisseur #${order.supplierId}`,
  }));
}

export function getOrders() {
  return apiGet("/api/orders");
}

export function getMyOrders() {
  return apiGet("/api/orders/mine");
}

export function confirmOrder(id, expectedDeliveryDate) {
  const query = expectedDeliveryDate ? `?expectedDeliveryDate=${expectedDeliveryDate}` : "";
  return apiPut(`/api/orders/${id}/confirm${query}`);
}

// Cree un brouillon (DRAFT) a partir d'une demande deja approuvee.
export function createOrder(order) {
  return apiPost("/api/orders", order);
}

// Ex : DRAFT -> SENT. order-service refuse (409) un changement hors du cycle de vie.
export function changeOrderStatus(id, status) {
  return apiPut(`/api/orders/${id}/status?status=${status}`);
}

// Actions possibles selon le statut actuel (meme cycle de vie que order-service)
export const ORDER_ACTIONS = {
  DRAFT: [{ status: "SENT", label: "Envoyer au fournisseur" }],
  SENT: [
    { status: "CONFIRMED", label: "Confirmer" },
    { status: "DELIVERED", label: "Marquer livrée" },
  ],
  CONFIRMED: [{ status: "DELIVERED", label: "Marquer livrée" }],
};

export const canCancelOrder = (order) => ["DRAFT", "SENT", "CONFIRMED"].includes(order.status);
