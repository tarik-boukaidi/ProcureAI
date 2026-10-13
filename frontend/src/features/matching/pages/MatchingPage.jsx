import { useEffect, useState } from "react";
import { CircleCheck, GitCompare, Percent, TriangleAlert } from "lucide-react";
import MatchDetail from "../components/MatchDetail";
import StatCard from "../../../shared/components/StatCard";
import StatusFilter, { applyFilter } from "../../../shared/components/StatusFilter";
import { loadMatchResults } from "../services/matchingService";
import { MATCH_STATUS, statusOf } from "../../../shared/constants/status";
import { formatDateTime, formatMoney } from "../../../shared/utils/format";
import "./MatchingPage.css";

const FILTERS = [
  { value: "ALL", label: "Tous" },
  { value: "MATCHED", label: "Conformes", test: (r) => r.status === "MATCHED" },
  { value: "MISMATCH", label: "Écarts", test: (r) => r.status === "MISMATCH" },
];

export default function MatchingPage() {
  const [results, setResults] = useState(null);
  const [error, setError] = useState("");
  const [filter, setFilter] = useState("ALL");
  const [selected, setSelected] = useState(null);

  useEffect(() => {
    loadMatchResults()
      .then(setResults)
      .catch((err) => setError(err.message));
  }, []);

  if (error) return <p className="page-error">Impossible de charger les rapprochements. {error}</p>;
  if (!results) return <p className="page-loading">Chargement…</p>;

  const matched = results.filter((r) => r.status === "MATCHED").length;
  const rate = results.length === 0 ? 0 : Math.round((matched / results.length) * 100);
  const visible = applyFilter(FILTERS, filter, results).sort((a, b) => b.id - a.id);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Rapprochement</h1>
          <p className="page-subtitle">Facture, commande et réception comparées ligne par ligne.</p>
        </div>
      </div>

      <div className="stats stats-3">
        <StatCard icon={Percent} label="Taux de conformité" value={`${rate} %`} hint={`${results.length} facture(s) vérifiée(s)`} />
        <StatCard icon={CircleCheck} label="Conformes" value={matched} hint="Validées automatiquement" />
        <StatCard icon={TriangleAlert} label="Avec écarts" value={results.length - matched} hint="Rejetées, à revoir avec le fournisseur" />
      </div>

      <div className="card table-card">
        <div className="toolbar">
          <StatusFilter filters={FILTERS} items={results} value={filter} onChange={setFilter} />
        </div>

        {results.length === 0 ? (
          <div className="empty-state">
            <GitCompare size={28} strokeWidth={1.5} />
            <h3>Aucun rapprochement</h3>
            <p>Lancez un rapprochement depuis la page Factures pour vérifier une facture reçue.</p>
          </div>
        ) : visible.length === 0 ? (
          <p className="empty">Aucun rapprochement dans cette catégorie.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Facture</th>
                <th>Fournisseur</th>
                <th>Commande</th>
                <th>Vérifié le</th>
                <th className="right">Montant HT (MAD)</th>
                <th className="right">Écarts</th>
                <th>Résultat</th>
              </tr>
            </thead>
            <tbody>
              {visible.map((result) => {
                const status = statusOf(MATCH_STATUS, result.status);
                return (
                  <tr key={result.id} className="clickable" onClick={() => setSelected(result)}>
                    <td className="mono">{result.invoice?.invoiceNumber ?? `#${result.invoiceId}`}</td>
                    <td>{result.invoice?.supplierName ?? "—"}</td>
                    <td className="mono muted">{result.invoice?.orderNumber ?? `#${result.purchaseOrderId}`}</td>
                    <td className="muted">{formatDateTime(result.checkedAt)}</td>
                    <td className="mono right">{result.invoice ? formatMoney(result.invoice.totalAmount) : "—"}</td>
                    <td className="mono right">{result.discrepancies.length}</td>
                    <td><span className={`tag tag-${status.tone}`}>{status.label}</span></td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>

      {selected && <MatchDetail result={selected} onClose={() => setSelected(null)} />}
    </div>
  );
}
