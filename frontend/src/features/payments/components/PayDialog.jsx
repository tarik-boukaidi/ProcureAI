import { useState } from "react";
import { Wallet } from "lucide-react";
import { payInvoice } from "../services/paymentService";
import { PAYMENT_METHOD } from "../../../shared/constants/status";
import { formatMoney } from "../../../shared/utils/format";
import "../../../shared/components/ConfirmDialog.css";

// Popup pour payer une facture validee : on choisit le moyen et la date
export default function PayDialog({ invoice, onClose, onPaid }) {
  const [method, setMethod] = useState("BANK_TRANSFER");
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handlePay() {
    setLoading(true);
    setError("");
    try {
      await payInvoice(invoice.id, method, date);
      onPaid();
    } catch (err) {
      setError(err.status === 409 ? "Cette facture est déjà payée ou n'est plus validée." : "Paiement impossible. Réessayez dans un instant.");
      setLoading(false);
    }
  }

  return (
    <div className="dialog-overlay" onClick={loading ? undefined : onClose}>
      <div className="dialog" role="dialog" aria-modal="true" onClick={(e) => e.stopPropagation()}>
        <div className="dialog-icon dialog-icon-green">
          <Wallet size={20} />
        </div>
        <h2 className="dialog-title">Payer la facture {invoice.invoiceNumber}</h2>
        <p className="dialog-message">
          {invoice.supplierName} — <strong className="mono">{formatMoney(invoice.totalTtc)} MAD TTC</strong>
        </p>

        <div className="form-row dialog-fields">
          <label className="field">
            <span>Moyen de paiement</span>
            <select value={method} onChange={(e) => setMethod(e.target.value)}>
              {Object.entries(PAYMENT_METHOD).map(([value, label]) => (
                <option key={value} value={value}>{label}</option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>Date</span>
            <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
          </label>
        </div>

        {error && <p className="dialog-error">{error}</p>}

        <div className="dialog-actions">
          <button className="btn btn-outline" onClick={onClose} disabled={loading}>Annuler</button>
          <button className="btn btn-dark" onClick={handlePay} disabled={loading}>
            {loading ? "Paiement…" : "Confirmer le paiement"}
          </button>
        </div>
      </div>
    </div>
  );
}
