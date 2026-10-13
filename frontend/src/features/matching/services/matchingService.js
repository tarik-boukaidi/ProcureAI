import { apiGet } from "../../../api/client";
import { loadInvoices } from "../../invoices/services/invoiceService";

// Resultats du rapprochement avec la facture complete (n°, fournisseur, lignes)
export async function loadMatchResults() {
  const [results, invoices] = await Promise.all([apiGet("/api/matching"), loadInvoices()]);
  const invoicesById = Object.fromEntries(invoices.map((i) => [i.id, i]));

  return results.map((result) => ({ ...result, invoice: invoicesById[result.invoiceId] }));
}
