import { apiGet } from "../../../api/client";
import { getSupplierNames } from "../../suppliers/services/supplierService";

// Solde global, rapport mensuel de l'annee et journal des ecritures
export async function loadAccounting(year) {
  const [balance, report, entries, supplierNames] = await Promise.all([
    apiGet("/api/accounting/outstanding"),
    apiGet(`/api/accounting/report?year=${year}`),
    apiGet("/api/accounting/entries"),
    getSupplierNames(),
  ]);

  return {
    balance,
    report,
    entries: entries.map((entry) => ({
      ...entry,
      supplierName: supplierNames[entry.supplierId] ?? `Fournisseur #${entry.supplierId}`,
    })),
  };
}
