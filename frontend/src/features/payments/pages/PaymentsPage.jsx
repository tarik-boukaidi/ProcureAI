import { useEffect, useState } from "react";
import { CalendarClock, CircleCheck, Wallet } from "lucide-react";
import PayDialog from "../components/PayDialog";
import StatCard from "../../../shared/components/StatCard";
import { loadPayments } from "../services/paymentService";
import { getCurrentUser } from "../../auth/services/authService";
import { PAYMENT_METHOD } from "../../../shared/constants/status";
import { formatDate, formatMoney, isOverdue } from "../../../shared/utils/format";

const sumOf = (items, field) => items.reduce((total, item) => total + Number(item[field]), 0);

export default function PaymentsPage() {
  // Seul le comptable paie : l'admin consulte (separation des taches)
  const canPay = getCurrentUser().role === "COMPTABLE";
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [paying, setPaying] = useState(null);
  const [message, setMessage] = useState("");

  function refresh() {
    loadPayments()
      .then(setData)
      .catch((err) => setError(err.message));
  }

  useEffect(() => {
    refresh();
  }, []);

  function handlePaid() {
    setMessage(`Facture ${paying.invoiceNumber} payée.`);
    setPaying(null);
    refresh();
  }

  if (error) return <p className="page-error">Impossible de charger les paiements. {error}</p>;
  if (!data) return <p className="page-loading">Chargement…</p>;

  const year = new Date().getFullYear();
  const paidThisYear = data.payments.filter((p) => p.paymentDate?.startsWith(String(year)));
  const lateCount = data.toPay.filter((i) => isOverdue(i.dueDate)).length;
  const history = [...data.payments].sort((a, b) => b.id - a.id);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Paiements</h1>
          <p className="page-subtitle">
            {canPay
              ? "Seules les factures validées par le rapprochement peuvent être payées."
              : "Consultation : les paiements sont faits par le comptable."}
          </p>
        </div>
      </div>

      {message && (
        <div className="form-success" role="status">
          <CircleCheck size={16} /> {message}
        </div>
      )}

      <div className="stats stats-3">
        <StatCard icon={Wallet} label="À payer" value={`${formatMoney(sumOf(data.toPay, "totalTtc"))} MAD`} hint={`${data.toPay.length} facture(s) validée(s)`} />
        <StatCard icon={CalendarClock} label="En retard" value={lateCount} hint="Échéance dépassée" />
        <StatCard icon={CircleCheck} label={`Payé en ${year}`} value={`${formatMoney(sumOf(paidThisYear, "amount"))} MAD`} hint={`${paidThisYear.length} paiement(s)`} />
      </div>

      <div className="card table-card card-gap">
        <div className="card-head">
          <div>
            <h2>À payer</h2>
            <span className="card-subtitle">La plus urgente en premier</span>
          </div>
        </div>

        {data.toPay.length === 0 ? (
          <p className="empty">Aucune facture à payer pour le moment.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>N° facture</th>
                <th>Fournisseur</th>
                <th>Échéance</th>
                <th className="right">Montant TTC (MAD)</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {data.toPay.map((invoice) => {
                const late = isOverdue(invoice.dueDate);
                return (
                  <tr key={invoice.id}>
                    <td className="mono">{invoice.invoiceNumber}</td>
                    <td>{invoice.supplierName}</td>
                    <td className={late ? "text-red" : "muted"}>
                      {formatDate(invoice.dueDate)} {late && <span className="tag tag-red">En retard</span>}
                    </td>
                    <td className="mono right">{formatMoney(invoice.totalTtc)}</td>
                    <td className="right">
                      {canPay && (
                        <button className="btn btn-sm btn-dark" onClick={() => setPaying(invoice)}>
                          <Wallet size={14} /> Payer
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

      <div className="card table-card">
        <div className="card-head">
          <div>
            <h2>Historique</h2>
            <span className="card-subtitle">Tous les paiements effectués</span>
          </div>
        </div>

        {history.length === 0 ? (
          <p className="empty">Aucun paiement pour le moment.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Référence</th>
                <th>Facture</th>
                <th>Fournisseur</th>
                <th>Date</th>
                <th>Moyen</th>
                <th className="right">Montant (MAD)</th>
              </tr>
            </thead>
            <tbody>
              {history.map((payment) => (
                <tr key={payment.id}>
                  <td className="mono">{payment.reference}</td>
                  <td className="mono muted">{payment.invoiceNumber}</td>
                  <td>{payment.supplierName}</td>
                  <td className="muted">{formatDate(payment.paymentDate)}</td>
                  <td>{PAYMENT_METHOD[payment.method] ?? payment.method}</td>
                  <td className="mono right">{formatMoney(payment.amount)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {paying && <PayDialog invoice={paying} onClose={() => setPaying(null)} onPaid={handlePaid} />}
    </div>
  );
}
