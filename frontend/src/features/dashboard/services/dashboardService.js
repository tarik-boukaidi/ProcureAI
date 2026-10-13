import { apiGet } from "../../../api/client";

// Tout ce dont le tableau de bord ADMIN a besoin, charge en parallele
export async function loadAdminDashboard(year) {
  const [outstanding, toPay, pendingDemandes, matches, report, invoices, suppliers] = await Promise.all([
    apiGet("/api/accounting/outstanding"),
    apiGet("/api/payments/to-pay"),
    apiGet("/api/demandes/status/PENDING"),
    apiGet("/api/matching"),
    apiGet(`/api/accounting/report?year=${year}`),
    apiGet("/api/invoices"),
    apiGet("/api/suppliers"),
  ]);

  const matched = matches.filter((m) => m.status === "MATCHED").length;
  const mismatched = matches.length - matched;

  // id du fournisseur -> nom, pour afficher le nom dans le tableau des factures
  const supplierNames = {};
  suppliers.forEach((s) => (supplierNames[s.id] = s.name));

  const recentInvoices = [...invoices]
    .sort((a, b) => b.id - a.id)
    .slice(0, 6)
    .map((invoice) => ({ ...invoice, supplierName: supplierNames[invoice.supplierId] ?? "—" }));

  return {
    outstanding,
    toPayCount: toPay.length,
    pendingCount: pendingDemandes.length,
    matched,
    mismatched,
    report,
    recentInvoices,
  };
}

// EMPLOYEE : ses demandes. MANAGER : toutes les demandes (il doit les approuver).
export function loadDemandes(role) {
  return apiGet(role === "MANAGER" ? "/api/demandes" : "/api/demandes/mine");
}
