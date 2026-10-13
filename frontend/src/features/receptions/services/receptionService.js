import { apiGet, apiPost } from "../../../api/client";
import { getSupplierNames } from "../../suppliers/services/supplierService";
import { getOrders, loadOrders } from "../../orders/services/orderService";

// Statuts de commande que reception-service accepte
const RECEIVABLE_STATUSES = ["SENT", "CONFIRMED", "DELIVERED"];

// Receptions avec le nom du fournisseur et le n° de la commande
export async function loadReceptions() {
  const [receptions, supplierNames, orders] = await Promise.all([
    apiGet("/api/receptions"),
    getSupplierNames(),
    getOrders(),
  ]);
  const orderNumbers = Object.fromEntries(orders.map((o) => [o.id, o.orderNumber]));

  return receptions.map((reception) => ({
    ...reception,
    supplierName: supplierNames[reception.supplierId] ?? `Fournisseur #${reception.supplierId}`,
    orderNumber: orderNumbers[reception.purchaseOrderId] ?? `#${reception.purchaseOrderId}`,
    state: receptionState(reception),
  }));
}

// Commandes qui attendent encore des articles.
// Reste a recevoir = commande - accepte (recu - refuse) dans les receptions deja faites :
// un article casse et refuse doit etre relivre par le fournisseur.
export async function loadOrdersToReceive() {
  const [orders, receptions] = await Promise.all([loadOrders(), apiGet("/api/receptions")]);

  return orders
    .filter((order) => RECEIVABLE_STATUSES.includes(order.status))
    .map((order) => {
      const accepted = {};
      receptions
        .filter((r) => r.purchaseOrderId === order.id)
        .flatMap((r) => r.lines)
        .forEach((l) => {
          accepted[l.productId] = (accepted[l.productId] ?? 0) + l.quantityReceived - l.quantityRejected;
        });
      const lines = order.lines.map((line) => ({
        ...line,
        remaining: Math.max(line.quantity - (accepted[line.productId] ?? 0), 0),
      }));
      return { ...order, lines };
    })
    .filter((order) => order.lines.some((line) => line.remaining > 0));
}

export function createReception(reception) {
  return apiPost("/api/receptions", reception);
}

// Avec refus : des articles ont ete refuses. Partielle : tout n'est pas encore arrive.
function receptionState(reception) {
  if (reception.lines.some((l) => l.quantityRejected > 0)) return "REJECTED";
  if (reception.lines.some((l) => l.quantityReceived < l.quantityOrdered)) return "PARTIAL";
  return "COMPLETE";
}

export const RECEPTION_STATE = {
  COMPLETE: { label: "Complète", tone: "green" },
  PARTIAL: { label: "Partielle", tone: "amber" },
  REJECTED: { label: "Avec refus", tone: "red" },
};
