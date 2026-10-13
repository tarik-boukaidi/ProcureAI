import { apiGet, apiPost } from "../../../api/client";
import { getSupplierNames } from "../../suppliers/services/supplierService";
import { getInvoices } from "../../invoices/services/invoiceService";

// Factures a payer + historique des paiements, avec les noms des fournisseurs et les n° de facture
export async function loadPayments() {
  const [toPay, payments, invoices, supplierNames] = await Promise.all([
    apiGet("/api/payments/to-pay"),
    apiGet("/api/payments"),
    getInvoices(),
    getSupplierNames(),
  ]);
  const invoiceNumbers = Object.fromEntries(invoices.map((i) => [i.id, i.invoiceNumber]));
  const supplierName = (id) => supplierNames[id] ?? `Fournisseur #${id}`;

  return {
    toPay: toPay.map((invoice) => ({ ...invoice, supplierName: supplierName(invoice.supplierId) })),
    payments: payments.map((payment) => ({
      ...payment,
      supplierName: supplierName(payment.supplierId),
      invoiceNumber: invoiceNumbers[payment.invoiceId] ?? `#${payment.invoiceId}`,
    })),
  };
}

// method : BANK_TRANSFER, CHECK ou CARD. Le montant est celui de la facture (choisi par le backend).
export function payInvoice(invoiceId, method, paymentDate) {
  return apiPost("/api/payments", { invoiceId, method, paymentDate });
}
