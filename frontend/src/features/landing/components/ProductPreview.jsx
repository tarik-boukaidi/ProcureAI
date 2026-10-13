import { BookOpen, Check, FileText, Inbox, PackageCheck, Search, ShoppingCart, TriangleAlert, Wallet } from "lucide-react";

// Maquette de l'application : liste des factures + panneau de rapprochement

const MENU = [
  { icon: Inbox, label: "Demandes", count: 4 },
  { icon: ShoppingCart, label: "Commandes" },
  { icon: PackageCheck, label: "Réceptions" },
  { icon: FileText, label: "Factures", count: 5, active: true },
  { icon: Wallet, label: "Paiements" },
  { icon: BookOpen, label: "Comptabilité" },
];

const INVOICES = [
  { number: "FAC-2026-014", supplier: "Atlas Informatique", order: "PO-0412", amount: "1 547,40", status: "À rapprocher", tone: "amber", selected: true },
  { number: "FAC-2026-013", supplier: "Bureau Plus", order: "PO-0409", amount: "864,00", status: "Validée", tone: "green" },
  { number: "FAC-2026-012", supplier: "Rif Emballages", order: "PO-0405", amount: "12 300,00", status: "Écart", tone: "red" },
  { number: "FAC-2026-011", supplier: "Souss Logistique", order: "PO-0401", amount: "4 120,80", status: "Payée", tone: "ink" },
  { number: "FAC-2026-010", supplier: "Médina Print", order: "PO-0398", amount: "2 016,00", status: "Payée", tone: "ink" },
];

const MATCH = [
  { label: "Commande", ref: "PO-0412", detail: "3 × 430,00 HT" },
  { label: "Réception", ref: "REC-0388", detail: "3 reçus · 0 rejeté" },
  { label: "Facture", ref: "FAC-2026-014", detail: "3 × 429,83 HT" },
];

export default function ProductPreview() {
  return (
    <div className="app">
      <aside className="app-sidebar">
        <div className="app-company">
          <span className="app-company-badge">AT</span>
          Atlas Distribution
        </div>
        <ul>
          {MENU.map(({ icon: Icon, label, count, active }) => (
            <li key={label} className={active ? "active" : ""}>
              <Icon size={15} strokeWidth={1.75} />
              <span>{label}</span>
              {count && <span className="app-count">{count}</span>}
            </li>
          ))}
        </ul>
      </aside>

      <div className="app-main">
        <div className="app-topbar">
          <h3>Factures</h3>
          <div className="app-search"><Search size={14} /> Rechercher une facture…</div>
          <button className="app-button">Importer une facture</button>
        </div>
        <div className="app-filters">
          <span className="active">Toutes <b>24</b></span>
          <span>À rapprocher <b>5</b></span>
          <span>Validées <b>12</b></span>
          <span>Payées <b>7</b></span>
        </div>
        <table className="app-table">
          <thead>
            <tr>
              <th>N°</th>
              <th>Fournisseur</th>
              <th>Commande</th>
              <th className="right">Montant TTC</th>
              <th>Statut</th>
            </tr>
          </thead>
          <tbody>
            {INVOICES.map((invoice) => (
              <tr key={invoice.number} className={invoice.selected ? "selected" : ""}>
                <td className="mono">{invoice.number}</td>
                <td>{invoice.supplier}</td>
                <td className="mono muted">{invoice.order}</td>
                <td className="mono right">{invoice.amount}</td>
                <td><span className={`tag tag-${invoice.tone}`}>{invoice.status}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <aside className="app-panel">
        <span className="app-panel-label">Rapprochement 3 voies</span>
        <h4 className="mono">FAC-2026-014</h4>
        <ul className="match-list">
          {MATCH.map((item) => (
            <li key={item.label}>
              <span className="match-check"><Check size={12} strokeWidth={3} /></span>
              <div>
                <strong>{item.label}</strong> <span className="mono muted">{item.ref}</span>
                <p className="mono">{item.detail}</p>
              </div>
            </li>
          ))}
        </ul>
        <div className="match-result">
          <TriangleAlert size={15} />
          <p>Écart de prix <b className="mono">−0,04 %</b>, dans la tolérance de 2 %.</p>
        </div>
        <button className="app-validate">Valider la facture</button>
      </aside>
    </div>
  );
}
