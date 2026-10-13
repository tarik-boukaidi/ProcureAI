import { useEffect, useState } from "react";
import { CircleCheck, FileUp, ScanText, TriangleAlert, X } from "lucide-react";
import { createInvoice, loadOrdersToInvoice, readInvoiceFile, VAT_RATES } from "../services/invoiceService";
import { formatMoney } from "../../../shared/utils/format";
import "./InvoiceDrawer.css";

const round2 = (n) => Math.round(n * 100) / 100;
const differs = (read, computed) => read != null && Math.abs(read - computed) > 0.01;

// Ce que l'OCR a lu sur la facture vs ce qui est saisi : une phrase par ecart
function scanGaps(scan, order, totals) {
  const gaps = [];
  if (scan.supplierIce && order.supplierIce && scan.supplierIce !== order.supplierIce) {
    gaps.push(`ICE lu ${scan.supplierIce} ≠ ICE de ${order.supplierName} (${order.supplierIce}) : la facture vient-elle du bon fournisseur ?`);
  }
  if (scan.supplierIce && !order.supplierIce) {
    gaps.push(`ICE lu ${scan.supplierIce}, mais ${order.supplierName} n'a pas d'ICE dans sa fiche : impossible de vérifier.`);
  }
  if (differs(scan.totalHT, totals.ht)) gaps.push(`Total HT lu ${formatMoney(scan.totalHT)} ≠ ${formatMoney(totals.ht)} saisi.`);
  if (differs(scan.tva, totals.vat)) gaps.push(`TVA lue ${formatMoney(scan.tva)} ≠ ${formatMoney(totals.vat)} calculée.`);
  if (differs(scan.totalTTC, totals.ttc)) gaps.push(`Total TTC lu ${formatMoney(scan.totalTTC)} ≠ ${formatMoney(totals.ttc)} calculé.`);
  return gaps;
}

// Code HTTP renvoye par invoice-service -> message clair pour le comptable
function saveErrorMessage(status) {
  if (status === 409) return "Ce numéro de facture existe déjà pour ce fournisseur.";
  if (status === 400) return "Cette commande ne peut pas être facturée : vérifiez son statut et les lignes.";
  if (status === 403) return "Seul un comptable peut saisir une facture.";
  return "Enregistrement impossible. Réessayez dans un instant.";
}

function ocrErrorMessage(status) {
  if (status === 400) return "Fichier illisible : envoyez un PDF, PNG ou JPG net.";
  return "La lecture automatique n'est pas disponible. Saisissez la facture à la main.";
}

const today = () => new Date().toISOString().slice(0, 10);

// Panneau a droite : le comptable importe la facture du fournisseur, l'OCR pre-remplit le formulaire,
// le comptable verifie puis enregistre. Le rapprochement se fait ensuite depuis la liste.
export default function InvoiceDrawer({ onClose, onSaved }) {
  const [orders, setOrders] = useState(null);
  const [order, setOrder] = useState(null);
  // Une ligne par produit de la commande : { productId, productName, quantity, unitPrice }
  const [lines, setLines] = useState([]);
  const [form, setForm] = useState({ invoiceNumber: "", invoiceDate: today(), vatRate: 20 });
  // Brouillon lu par l'OCR (null tant qu'aucun fichier n'est importe)
  const [scan, setScan] = useState(null);
  const [fileName, setFileName] = useState("");
  const [reading, setReading] = useState(false);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    loadOrdersToInvoice()
      .then(setOrders)
      .catch(() => setError("Impossible de charger les commandes."));
  }, []);

  async function handleFile(event) {
    const file = event.target.files[0];
    if (!file) return;
    setFileName(file.name);
    setError("");
    setScan(null);
    setReading(true);
    try {
      const result = await readInvoiceFile(file);
      setScan(result);
      // Taux deduit de la facture : TVA / HT (ex : 1 440 / 7 200 = 20 %)
      const readRate = result.tva != null && result.totalHT ? Math.round((result.tva / result.totalHT) * 100) : null;
      // On ne remplace que ce que l'OCR a trouve : le reste reste saisi a la main
      setForm({
        invoiceNumber: result.invoiceNumber ?? form.invoiceNumber,
        invoiceDate: result.invoiceDate ?? form.invoiceDate,
        vatRate: VAT_RATES.includes(readRate) ? readRate : form.vatRate,
      });
    } catch (err) {
      setError(ocrErrorMessage(err.status));
    } finally {
      setReading(false);
      event.target.value = "";
    }
  }

  function selectOrder(id) {
    const selected = orders.find((o) => o.id === Number(id)) ?? null;
    setOrder(selected);
    // Par defaut, la facture reprend la commande : le comptable corrige si le fournisseur facture autre chose
    setLines(
      (selected?.lines ?? []).map((line) => ({
        productId: line.productId,
        productName: line.productName,
        quantity: line.quantity,
        unitPrice: line.unitPrice,
      }))
    );
  }

  function updateLine(index, field, value) {
    setLines(lines.map((line, i) => (i === index ? { ...line, [field]: Number(value) } : line)));
  }

  // Meme calcul que invoice-service
  const ht = round2(lines.reduce((sum, line) => sum + line.quantity * line.unitPrice, 0));
  const vat = round2((ht * form.vatRate) / 100);
  const totals = { ht, vat, ttc: round2(ht + vat) };
  const gaps = scan && order ? scanGaps(scan, order, totals) : [];

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
      onSaved(invoice);
    } catch (err) {
      setError(saveErrorMessage(err.status));
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="drawer-overlay" onClick={onClose}>
      {/* stopPropagation : un clic dans le panneau ne doit pas le fermer */}
      <form className="drawer drawer-wide" onClick={(e) => e.stopPropagation()} onSubmit={handleSubmit}>
        <header className="drawer-header">
          <h2>Nouvelle facture</h2>
          <button type="button" className="icon-button" onClick={onClose} title="Fermer">
            <X size={18} />
          </button>
        </header>

        <div className="drawer-body">
          {error && (
            <div className="form-error" role="alert">
              <TriangleAlert size={16} /> {error}
            </div>
          )}

          <label className={reading ? "invoice-upload reading" : "invoice-upload"}>
            <input type="file" accept=".pdf,.png,.jpg,.jpeg" onChange={handleFile} disabled={reading} />
            {reading ? <ScanText size={22} /> : <FileUp size={22} />}
            <span className="invoice-upload-title">
              {reading ? "Lecture de la facture…" : fileName || "Importer la facture du fournisseur"}
            </span>
            <span className="invoice-upload-hint">
              {reading ? "Quelques secondes" : "PDF, PNG ou JPG : numéro, date, montants et ICE sont lus automatiquement"}
            </span>
          </label>

          {scan && (
            <div className="form-success" role="status">
              <CircleCheck size={16} />
              <span>
                Facture lue. Choisissez la commande : les montants et l'ICE lus seront comparés à votre saisie.
              </span>
            </div>
          )}

          <div className="form-row">
            <label className="field">
              <span>N° de facture *</span>
              <input
                value={form.invoiceNumber}
                onChange={(e) => setForm({ ...form, invoiceNumber: e.target.value })}
                placeholder="FA-2026-0042"
                required
              />
            </label>
            <label className="field">
              <span>Date de facture *</span>
              <input type="date" value={form.invoiceDate} onChange={(e) => setForm({ ...form, invoiceDate: e.target.value })} required />
            </label>
          </div>

          <label className="field">
            <span>Commande facturée *</span>
            <select value={order?.id ?? ""} onChange={(e) => selectOrder(e.target.value)} required disabled={!orders}>
              <option value="" disabled>{orders ? "Choisir une commande…" : "Chargement…"}</option>
              {orders?.map((o) => (
                <option key={o.id} value={o.id}>{o.orderNumber} — {o.supplierName}</option>
              ))}
            </select>
          </label>

          {order && (
            <>
              <table className="table invoice-lines">
                <thead>
                  <tr>
                    <th>Produit</th>
                    <th className="right">Qté</th>
                    <th className="right">Prix unit. HT</th>
                    <th className="right">Total HT</th>
                  </tr>
                </thead>
                <tbody>
                  {lines.map((line, index) => (
                    <tr key={line.productId}>
                      <td>{line.productName || `Produit #${line.productId}`}</td>
                      <td className="right">
                        <input type="number" min="0" value={line.quantity} onChange={(e) => updateLine(index, "quantity", e.target.value)} />
                      </td>
                      <td className="right">
                        <input
                          type="number"
                          min="0"
                          step="0.01"
                          value={line.unitPrice}
                          onChange={(e) => updateLine(index, "unitPrice", e.target.value)}
                        />
                      </td>
                      <td className="mono right">{formatMoney(line.quantity * line.unitPrice)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>

              <div className="detail-subtotal">
                <span>Total HT</span>
                <span className="mono">{formatMoney(totals.ht)} MAD</span>
              </div>
              <div className="detail-subtotal invoice-vat">
                <label>
                  TVA
                  <select value={form.vatRate} onChange={(e) => setForm({ ...form, vatRate: Number(e.target.value) })}>
                    {VAT_RATES.map((rate) => (
                      <option key={rate} value={rate}>{rate} %</option>
                    ))}
                  </select>
                </label>
                <span className="mono">{formatMoney(totals.vat)} MAD</span>
              </div>
              <div className="detail-total">
                <span>Total TTC</span>
                <span className="mono">{formatMoney(totals.ttc)} MAD</span>
              </div>

              {gaps.length > 0 && (
                <div className="invoice-warning" role="alert">
                  <TriangleAlert size={14} />
                  <ul>
                    {gaps.map((gap) => (
                      <li key={gap}>{gap}</li>
                    ))}
                  </ul>
                </div>
              )}
              {scan && gaps.length === 0 && (
                <p className="invoice-ok">
                  <CircleCheck size={14} /> Aucun écart avec ce qui a été lu sur la facture.
                </p>
              )}

              <p className="drawer-hint">
                Les lignes reprennent la commande : modifiez-les pour qu'elles correspondent à la facture du fournisseur.
                Le rapprochement signalera les écarts avec la commande et la réception.
              </p>
            </>
          )}
        </div>

        <footer className="drawer-footer">
          <button type="button" className="btn btn-outline" onClick={onClose}>Annuler</button>
          <button type="submit" className="btn btn-dark" disabled={saving || reading || !order}>
            {saving ? "Enregistrement…" : "Enregistrer la facture"}
          </button>
        </footer>
      </form>
    </div>
  );
}
