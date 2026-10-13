import { useEffect, useState } from "react";
import { BookOpen, CircleCheck, FileText, Wallet } from "lucide-react";
import StatCard from "../../../shared/components/StatCard";
import MonthlyChart from "../../../shared/components/MonthlyChart";
import StatusFilter, { applyFilter } from "../../../shared/components/StatusFilter";
import { loadAccounting } from "../services/accountingService";
import { ACCOUNTS } from "../../../shared/constants/status";
import { formatFullDate, formatMoney } from "../../../shared/utils/format";

const FILTERS = [
  { value: "ALL", label: "Toutes" },
  { value: "INVOICE_RECORDED", label: "Factures", test: (e) => e.type === "INVOICE_RECORDED" },
  { value: "VAT_RECORDED", label: "TVA", test: (e) => e.type === "VAT_RECORDED" },
  { value: "PAYMENT_RECORDED", label: "Paiements", test: (e) => e.type === "PAYMENT_RECORDED" },
];

// L'annee en cours et les 2 precedentes
const CURRENT_YEAR = new Date().getFullYear();
const YEARS = [CURRENT_YEAR, CURRENT_YEAR - 1, CURRENT_YEAR - 2];

// "6111" -> "6111 Achats"
const account = (code) => `${code} ${ACCOUNTS[code] ?? ""}`;

export default function AccountingPage() {
  const [year, setYear] = useState(CURRENT_YEAR);
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [filter, setFilter] = useState("ALL");

  useEffect(() => {
    loadAccounting(year)
      .then(setData)
      .catch((err) => setError(err.message));
  }, [year]);

  if (error) return <p className="page-error">Impossible de charger la comptabilité. {error}</p>;
  if (!data) return <p className="page-loading">Chargement…</p>;

  const entries = applyFilter(FILTERS, filter, data.entries).sort((a, b) => b.id - a.id);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Comptabilité</h1>
          <p className="page-subtitle">Écritures générées automatiquement à chaque facture validée et chaque paiement.</p>
        </div>
        <label className="field year-select">
          <select value={year} onChange={(e) => setYear(Number(e.target.value))}>
            {YEARS.map((y) => (
              <option key={y} value={y}>{y}</option>
            ))}
          </select>
        </label>
      </div>

      <div className="stats stats-3">
        <StatCard icon={FileText} label="Total facturé" value={`${formatMoney(data.balance.invoiced)} MAD`} hint="Factures validées, TTC (compte 4411)" />
        <StatCard icon={CircleCheck} label="Total payé" value={`${formatMoney(data.balance.paid)} MAD`} hint="Paiements enregistrés" />
        <StatCard icon={Wallet} label="Reste à payer" value={`${formatMoney(data.balance.remaining)} MAD`} hint="Dette fournisseurs" />
      </div>

      <div className="card-gap">
        <MonthlyChart report={data.report} year={year} />
      </div>

      <div className="card table-card">
        <div className="card-head">
          <div>
            <h2>Journal des écritures</h2>
            <span className="card-subtitle">Plan comptable marocain (CGNC)</span>
          </div>
          <StatusFilter filters={FILTERS} items={data.entries} value={filter} onChange={setFilter} />
        </div>

        {data.entries.length === 0 ? (
          <div className="empty-state">
            <BookOpen size={28} strokeWidth={1.5} />
            <h3>Aucune écriture</h3>
            <p>Une écriture est créée quand une facture est validée, puis quand elle est payée.</p>
          </div>
        ) : entries.length === 0 ? (
          <p className="empty">Aucune écriture dans cette catégorie.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Libellé</th>
                <th>Fournisseur</th>
                <th>Débit</th>
                <th>Crédit</th>
                <th className="right">Montant (MAD)</th>
              </tr>
            </thead>
            <tbody>
              {entries.map((entry) => (
                <tr key={entry.id}>
                  <td className="muted">{formatFullDate(entry.entryDate)}</td>
                  <td>{entry.description}</td>
                  <td>{entry.supplierName}</td>
                  <td className="mono muted">{account(entry.debitAccount)}</td>
                  <td className="mono muted">{account(entry.creditAccount)}</td>
                  <td className="mono right">{formatMoney(entry.amount)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
