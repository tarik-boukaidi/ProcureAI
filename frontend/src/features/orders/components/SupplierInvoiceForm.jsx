import { useEffect, useState } from "react";
import { TriangleAlert } from "lucide-react";
import { createInvoice, VAT_RATES } from "../../invoices/services/invoiceService";
import { apiGet } from "../../../api/client";
import { formatMoney } from "../../../shared/utils/format";

const today = () => new Date().toISOString().slice(0, 10);
const round2 = (n) => Math.round(n * 100) / 100;

function errorMessage(status) {
  if (status === 409) return "Ce numéro de facture existe déjà.";
  if (status === 400) return "Cette commande ne peut pas encore être facturée.";
  return "Envoi impossible. Réessayez dans un instant.";
}

// Le fournisseur depose sa facture pour la commande ouverte.
export default function SupplierInvoiceForm({ order }) {
  const [lines, setLines] = useState(
    order.lines.map((line) => ({
      productId: line.productId,
      productName: line.productName,
      quantity: line.quantity,
      unitPrice: line.unitPrice,
    }))
  );
  const [form, setForm] = useState({ invoiceNumber: "", invoiceDate: today(), vatRate: 20 });
  const [existing, setExisting] = useState([]);
  const [error, setError] = useState("");
  const [savedNumber, setSavedNumber] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    apiGet(`/api/invoices/order/${order.id}`)
      .then(setExisting)
      .catch(() => setExisting([]));
  }, [order.id]);

  function updateLine(index, field, value) {
    setLines(lines.map((line, i) => (i === index ? { ...line, [field]: Number(value) } : line)));
  }

  const ht = round2(lines.reduce((sum, line) => sum + line.quantity * line.unitPrice, 0));
  const vat = round2((ht * form.vatRate) / 100);
  const ttc = round2(ht + vat);

  async function handleSubmit(event) {
    event.preventDefault();
    const invoiced = lines.filter((line) => line.quantity > 0);
    if (invoiced.length === 0) {
      setError("Indiquez au moins une ligne facturée.");
      return;
    }
    setError("");
    setSaving(true);
    try {
      const invoice = await createInvoice({
        invoiceNumber: form.invoiceNumber.trim(),
        invoiceDate: form.invoiceDate,
        vatRate: form.vatRate,
        purchaseOrderId: order.id,
        supplierId: order.supplierId,
        lines: invoiced,
      });
      setSavedNumber(invoice.invoiceNumber);
      setExisting((current) => [...current, invoice]);
      setForm({ ...form, invoiceNumber: "" });
    } catch (err) {
      setError(errorMessage(err.status));
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <h3 className="detail-section">Déposer une facture</h3>
      {existing.length > 0 && (
        <p className="drawer-hint">
          Déjà déposée : {existing.map((invoice) => invoice.invoiceNumber).join(", ")}.
        </p>
      )}
      {savedNumber && <p className="form-success">Facture {savedNumber} envoyée. Le comptable va la rapprocher.</p>}
      {error && (
        <div className="form-error" role="alert">
          <TriangleAlert size={16} /> {error}
        </div>
      )}
      <div className="form-row">
        <label className="field">
          <span>N° de facture *</span>
          <input value={form.invoiceNumber} onChange={(e) => setForm({ ...form, invoiceNumber: e.target.value })} required />
        </label>
        <label className="field">
          <span>Date</span>
          <input type="date" value={form.invoiceDate} onChange={(e) => setForm({ ...form, invoiceDate: e.target.value })} required />
        </label>
      </div>
      <label className="field">
        <span>TVA</span>
        <select value={form.vatRate} onChange={(e) => setForm({ ...form, vatRate: Number(e.target.value) })}>
          {VAT_RATES.map((rate) => (
            <option key={rate} value={rate}>{rate} %</option>
          ))}
        </select>
      </label>
      {lines.map((line, index) => (
        <div className="form-row" key={line.productId}>
          <label className="field">
            <span>{line.productName}</span>
            <input type="number" min="0" value={line.quantity} onChange={(e) => updateLine(index, "quantity", e.target.value)} />
          </label>
          <label className="field">
            <span>Prix unitaire</span>
            <input type="number" min="0.01" step="0.01" value={line.unitPrice} onChange={(e) => updateLine(index, "unitPrice", e.target.value)} />
          </label>
        </div>
      ))}
      <p className="drawer-hint">Total TTC : {formatMoney(ttc)} MAD</p>
      <button type="submit" className="btn btn-create" disabled={saving}>
        {saving ? "Envoi…" : "Déposer la facture"}
      </button>
    </form>
  );
}
