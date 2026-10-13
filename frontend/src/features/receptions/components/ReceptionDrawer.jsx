import { useEffect, useState } from "react";
import { TriangleAlert, X } from "lucide-react";
import { createReception, loadOrdersToReceive } from "../services/receptionService";

// Code HTTP renvoye par reception-service -> message clair pour le magasinier
function errorMessage(status) {
  if (status === 400) return "Vérifiez les quantités : on ne peut pas recevoir plus que commandé, ni refuser plus que reçu.";
  if (status === 403) return "Seul un magasinier peut enregistrer une réception.";
  return "Enregistrement impossible. Réessayez dans un instant.";
}

const today = () => new Date().toISOString().slice(0, 10);

// Panneau a droite : le magasinier choisit la commande livree, compte les articles et note ceux qui sont casses
export default function ReceptionDrawer({ onClose, onSaved }) {
  const [orders, setOrders] = useState(null);
  const [order, setOrder] = useState(null);
  // Une ligne par produit de la commande : { productId, productName, remaining, received, rejected }
  const [lines, setLines] = useState([]);
  const [form, setForm] = useState({ deliveryNoteNumber: "", receivedDate: today(), comment: "" });
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    loadOrdersToReceive()
      .then(setOrders)
      .catch(() => setError("Impossible de charger les commandes en attente de livraison."));
  }, []);

  function selectOrder(id) {
    const selected = orders.find((o) => o.id === Number(id)) ?? null;
    setOrder(selected);
    // Par defaut : tout ce qui reste a recevoir est arrive, rien n'est casse
    setLines(
      (selected?.lines ?? [])
        .filter((line) => line.remaining > 0)
        .map((line) => ({ ...line, received: line.remaining, rejected: 0 }))
    );
  }

  function updateLine(index, field, value) {
    setLines(lines.map((line, i) => (i === index ? { ...line, [field]: Number(value) } : line)));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    const received = lines.filter((line) => line.received > 0);
    if (received.length === 0) {
      setError("Indiquez au moins un article reçu.");
      return;
    }

    setError("");
    setSaving(true);
    try {
      await createReception({
        ...form,
        purchaseOrderId: order.id,
        supplierId: order.supplierId,
        lines: received.map((line) => ({
          productId: line.productId,
          productName: line.productName,
          quantityReceived: line.received,
          quantityRejected: line.rejected,
        })),
      });
      onSaved();
    } catch (err) {
      setError(errorMessage(err.status));
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="drawer-overlay" onClick={onClose}>
      {/* stopPropagation : un clic dans le panneau ne doit pas le fermer */}
      <form className="drawer drawer-wide" onClick={(e) => e.stopPropagation()} onSubmit={handleSubmit}>
        <header className="drawer-header">
          <h2>Nouvelle réception</h2>
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

          {!orders ? (
            !error && <p className="page-loading">Chargement…</p>
          ) : orders.length === 0 ? (
            <p className="empty">Aucune commande n'attend de livraison pour le moment.</p>
          ) : (
            <>
              <label className="field">
                <span>Commande livrée *</span>
                <select value={order?.id ?? ""} onChange={(e) => selectOrder(e.target.value)} required autoFocus>
                  <option value="" disabled>Choisir une commande…</option>
                  {orders.map((o) => (
                    <option key={o.id} value={o.id}>{o.orderNumber} — {o.supplierName}</option>
                  ))}
                </select>
              </label>

              {order && (
                <>
                  <div className="form-row">
                    <label className="field">
                      <span>N° bon de livraison</span>
                      <input
                        value={form.deliveryNoteNumber}
                        onChange={(e) => setForm({ ...form, deliveryNoteNumber: e.target.value })}
                        placeholder="BL-2026-0042"
                      />
                    </label>
                    <label className="field">
                      <span>Reçue le</span>
                      <input type="date" value={form.receivedDate} onChange={(e) => setForm({ ...form, receivedDate: e.target.value })} required />
                    </label>
                  </div>

                  <table className="table reception-lines">
                    <thead>
                      <tr>
                        <th>Produit</th>
                        <th className="right">Attendu</th>
                        <th className="right">Reçu</th>
                        <th className="right">Refusé</th>
                      </tr>
                    </thead>
                    <tbody>
                      {lines.map((line, index) => (
                        <tr key={line.productId}>
                          <td>{line.productName || `Produit #${line.productId}`}</td>
                          <td className="mono right">{line.remaining}</td>
                          <td className="right">
                            <input
                              type="number"
                              min="0"
                              max={line.remaining}
                              value={line.received}
                              onChange={(e) => updateLine(index, "received", e.target.value)}
                            />
                          </td>
                          <td className="right">
                            <input
                              type="number"
                              min="0"
                              max={line.received}
                              value={line.rejected}
                              onChange={(e) => updateLine(index, "rejected", e.target.value)}
                            />
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>

                  <label className="field">
                    <span>Commentaire</span>
                    <textarea
                      rows="3"
                      value={form.comment}
                      onChange={(e) => setForm({ ...form, comment: e.target.value })}
                      placeholder="Ex : 1 chaise livrée avec le vérin cassé, refusée"
                    />
                  </label>

                  <p className="drawer-hint">
                    Refusé = article cassé ou non conforme. Il ne sera pas payé au fournisseur.
                  </p>
                </>
              )}
            </>
          )}
        </div>

        <footer className="drawer-footer">
          <button type="button" className="btn btn-outline" onClick={onClose}>Annuler</button>
          <button type="submit" className="btn btn-dark" disabled={saving || !order}>
            {saving ? "Enregistrement…" : "Enregistrer la réception"}
          </button>
        </footer>
      </form>
    </div>
  );
}
