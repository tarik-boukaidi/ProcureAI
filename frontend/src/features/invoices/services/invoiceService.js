import { apiGet, apiPost } from "../../../api/client";
import { getSupplierNames, getSuppliers } from "../../suppliers/services/supplierService";
import { getOrders } from "../../orders/services/orderService";

// invoice-service n'accepte une facture que pour une commande envoyee au fournisseur
const INVOICEABLE_ORDER_STATUSES = ["SENT", "CONFIRMED", "DELIVERED"];

export function getInvoices() {
  return apiGet("/api/invoices");
}

// Factures avec le nom du fournisseur et le n° de la commande
export async function loadInvoices() {
  const [invoices, supplierNames, orders] = await Promise.all([getInvoices(), getSupplierNames(), getOrders()]);
  const orderNumbers = Object.fromEntries(orders.map((o) => [o.id, o.orderNumber]));

  return invoices.map((invoice) => ({
    ...invoice,
    supplierName: supplierNames[invoice.supplierId] ?? `Fournisseur #${invoice.supplierId}`,
    orderNumber: orderNumbers[invoice.purchaseOrderId] ?? `#${invoice.purchaseOrderId}`,
  }));
}

// Taux de TVA marocains acceptes par invoice-service
export const VAT_RATES = [20, 14, 10, 7, 0];

// Commandes qu'on peut facturer, avec le nom et l'ICE du fournisseur
export async function loadOrdersToInvoice() {
  const [orders, suppliers] = await Promise.all([getOrders(), getSuppliers()]);
  const supplierById = Object.fromEntries(suppliers.map((s) => [s.id, s]));
  return orders
    .filter((order) => INVOICEABLE_ORDER_STATUSES.includes(order.status))
    .map((order) => ({
      ...order,
      supplierName: supplierById[order.supplierId]?.name ?? `Fournisseur #${order.supplierId}`,
      supplierIce: supplierById[order.supplierId]?.ice ?? null,
    }))
    .sort((a, b) => b.id - a.id);
}

// ai-service lit le PDF / la photo et renvoie un brouillon :
// { invoiceNumber, invoiceDate, supplierIce, totalHT, tva, totalTTC } (null si pas trouve)
export function readInvoiceFile(file) {
  const form = new FormData();
  form.append("file", file);
  return apiPost("/api/ai/ocr/invoice", form);
}

export function createInvoice(invoice) {
  return apiPost("/api/invoices", invoice);
}

// Rapprochement 3 voies : matching-service valide ou rejette la facture
export function matchInvoice(invoiceId) {
  return apiPost(`/api/matching/invoices/${invoiceId}`);
}
