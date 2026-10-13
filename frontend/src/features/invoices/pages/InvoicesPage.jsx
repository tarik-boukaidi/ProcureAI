import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { CircleCheck, FileText, GitCompare, Plus, Search, TriangleAlert } from "lucide-react";
import InvoiceDetail from "../components/InvoiceDetail";
import InvoiceDrawer from "../components/InvoiceDrawer";
import StatusFilter, { applyFilter } from "../../../shared/components/StatusFilter";
import { loadInvoices, matchInvoice } from "../services/invoiceService";
import { getCurrentUser } from "../../auth/services/authService";
import { INVOICE_STATUS, statusOf } from "../../../shared/constants/status";
import { formatDate, formatMoney, isOverdue } from "../../../shared/utils/format";

const FILTERS = [
  { value: "ALL", label: "Toutes" },
  { value: "RECEIVED", label: "À rapprocher", test: (i) => i.status === "RECEIVED" },
  { value: "VALIDATED", label: "Validées", test: (i) => i.status === "VALIDATED" },
  { value: "REJECTED", label: "Rejetées", test: (i) => i.status === "REJECTED" },
  { value: "PAID", label: "Payées", test: (i) => i.status === "PAID" },
];

export default function InvoicesPage() {
  // Seul le comptable saisit et rapproche : l'admin consulte (separation des taches)
  const canMatch = getCurrentUser().role === "COMPTABLE";
  const [creating, setCreating] = useState(false);
  const [invoices, setInvoices] = useState(null);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState("ALL");
  const [selected, setSelected] = useState(null);
  const [busyId, setBusyId] = useState(null);
  // Resultat du dernier rapprochement : { ok, text }
  const [message, setMessage] = useState(null);

  function refresh() {
    loadInvoices()
      .then(setInvoices)
      .catch((err) => setError(err.message));
  }

  useEffect(() => {
    refresh();
  }, []);

  async function handleMatch(invoice) {
    setMessage(null);
    setBusyId(invoice.id);
    try {
      const result = await matchInvoice(invoice.id);
      if (result.status === "MATCHED") {
        setMessage({ ok: true, text: `Facture ${invoice.invoiceNumber} conforme : elle est validée et prête à être payée.` });
      } else {
        setMessage({ ok: false, text: `Facture ${invoice.invoiceNumber} : ${result.discrepancies.length} écart(s) détecté(s), elle est rejetée.` });
      }
      refresh();
    } catch {
      setMessage({ ok: false, text: `Le rapprochement de la facture ${invoice.invoiceNumber} a échoué. Réessayez dans un instant.` });
    } finally {
      setBusyId(null);
    }
  }

  function handleSaved(invoice) {
    setCreating(false);
    setFilter("ALL");
    setMessage({ ok: true, text: `Facture ${invoice.invoiceNumber} enregistrée. Lancez le rapprochement pour la valider.` });
    refresh();
  }

  if (error) return <p className="page-error">Impossible de charger les factures. {error}</p>;
  if (!invoices) return <p className="page-loading">Chargement…</p>;

  const visible = applyFilter(FILTERS, filter, invoices)
    .filter((i) => `${i.invoiceNumber} ${i.supplierName} ${i.orderNumber}`.toLowerCase().includes(search.toLowerCase()))
    .sort((a, b) => b.id - a.id);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Factures</h1>
          <p className="page-subtitle">
            {canMatch
              ? "Chaque facture est rapprochée de sa commande et de sa réception avant paiement."
              : "Consultation : le rapprochement est fait par le comptable."}
          </p>
        </div>
        {canMatch && (
          <button className="btn btn-dark" onClick={() => setCreating(true)}>
            <Plus size={16} /> Nouvelle facture
          </button>
        )}
      </div>

      {message && (
        <div className={message.ok ? "form-success" : "form-error"} role="status">
          {message.ok ? <CircleCheck size={16} /> : <TriangleAlert size={16} />}
          <span>
            {message.text} {!message.ok && <Link to="/rapprochement" className="card-link">Voir les écarts</Link>}
          </span>
        </div>
      )}

      <div className="card table-card">
        <div className="toolbar">
          <StatusFilter filters={FILTERS} items={invoices} value={filter} onChange={setFilter} />
          <label className="search">
            <Search size={16} />
            <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Rechercher un n°, un fournisseur…" />
          </label>
        </div>

        {invoices.length === 0 ? (
          <div className="empty-state">
            <FileText size={28} strokeWidth={1.5} />
            <h3>Aucune facture</h3>
            <p>Les factures fournisseurs reçues apparaîtront ici, prêtes à être rapprochées.</p>
          </div>
        ) : visible.length === 0 ? (
          <p className="empty">Aucune facture ne correspond à votre recherche.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>N° facture</th>
                <th>Fournisseur</th>
                <th>Commande</th>
                <th>Date</th>
                <th>Échéance</th>
                <th className="right">Montant TTC (MAD)</th>
                <th>Statut</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {visible.map((invoice) => {
                const status = statusOf(INVOICE_STATUS, invoice.status);
                const late = invoice.status !== "PAID" && isOverdue(invoice.dueDate);
                return (
                  <tr key={invoice.id} className="clickable" onClick={() => setSelected(invoice)}>
                    <td className="mono">{invoice.invoiceNumber}</td>
                    <td>{invoice.supplierName}</td>
                    <td className="mono muted">{invoice.orderNumber}</td>
                    <td className="muted">{formatDate(invoice.invoiceDate)}</td>
                    <td className={late ? "text-red" : "muted"}>{formatDate(invoice.dueDate)}</td>
                    <td className="mono right">{formatMoney(invoice.totalTtc)}</td>
                    <td><span className={`tag tag-${status.tone}`}>{status.label}</span></td>
                    <td className="right">
                      {canMatch && invoice.status === "RECEIVED" && (
                        <button
                          className="btn btn-sm btn-dark"
                          disabled={busyId === invoice.id}
                          onClick={(e) => {
                            e.stopPropagation();
                            handleMatch(invoice);
                          }}
                        >
                          <GitCompare size={14} /> {busyId === invoice.id ? "Vérification…" : "Rapprocher"}
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>

      {selected && <InvoiceDetail invoice={selected} onClose={() => setSelected(null)} />}
      {creating && <InvoiceDrawer onClose={() => setCreating(false)} onSaved={handleSaved} />}
    </div>
  );
}
