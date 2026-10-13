import { useState } from "react";
import { TriangleAlert } from "lucide-react";
import DetailDrawer from "../../../shared/components/DetailDrawer";
import ConfirmDialog from "../../../shared/components/ConfirmDialog";
import { canCancelOrder, changeOrderStatus, ORDER_ACTIONS } from "../services/orderService";
import { ORDER_STATUS, statusOf } from "../../../shared/constants/status";
import { formatFullDate, formatMoney } from "../../../shared/utils/format";

// canEdit : l'admin fait avancer la commande (envoi, confirmation, livraison, annulation)
export default function OrderDetail({ order, canEdit, onClose, onChanged }) {
  const status = statusOf(ORDER_STATUS, order.status);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [confirmCancel, setConfirmCancel] = useState(false);

  const actions = canEdit ? ORDER_ACTIONS[order.status] ?? [] : [];
  const cancellable = canEdit && canCancelOrder(order);

  async function updateStatus(newStatus) {
    setBusy(true);
    setError("");
    try {
      onChanged(await changeOrderStatus(order.id, newStatus));
    } catch (err) {
      setError(err.status === 409
        ? "Ce changement n'est plus possible : la commande a déjà changé de statut."
        : "Mise à jour impossible. Réessayez dans un instant.");
    } finally {
      setBusy(false);
    }
  }

  const footer = (actions.length > 0 || cancellable) && (
    <>
      {cancellable && (
        <button className="btn btn-outline" onClick={() => setConfirmCancel(true)} disabled={busy}>
          Annuler la commande
        </button>
      )}
      {actions.map((action, index) => (
        <button
          key={action.status}
          className={index === actions.length - 1 ? "btn btn-dark" : "btn btn-outline"}
          onClick={() => updateStatus(action.status)}
          disabled={busy}
        >
          {action.label}
        </button>
      ))}
    </>
  );

  return (
    <DetailDrawer title={`Commande ${order.orderNumber}`} subtitle={order.supplierName} onClose={onClose} footer={footer}>
      {error && (
        <div className="form-error" role="alert">
          <TriangleAlert size={16} /> {error}
        </div>
      )}

      <dl className="detail-list">
        <dt>Statut</dt>
        <dd><span className={`tag tag-${status.tone}`}>{status.label}</span></dd>
        <dt>Demande d'origine</dt>
        <dd className="mono">#{order.purchaseRequestId}</dd>
        <dt>Date de commande</dt>
        <dd>{formatFullDate(order.orderDate)}</dd>
        <dt>Livraison prévue</dt>
        <dd>{formatFullDate(order.expectedDeliveryDate)}</dd>
      </dl>

      <h3 className="detail-section">Articles commandés</h3>
      <table className="table">
        <thead>
          <tr>
            <th>Produit</th>
            <th className="right">Qté</th>
            <th className="right">Prix unit.</th>
            <th className="right">Total</th>
          </tr>
        </thead>
        <tbody>
          {order.lines.map((line) => (
            <tr key={line.id}>
              <td>{line.productName || `Produit #${line.productId}`}</td>
              <td className="mono right">{line.quantity}</td>
              <td className="mono right">{formatMoney(line.unitPrice)}</td>
              <td className="mono right">{formatMoney(line.unitPrice * line.quantity)}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="detail-total">
        <span>Total</span>
        <span className="mono">{formatMoney(order.totalAmount)} MAD</span>
      </div>

      {canEdit && order.status === "DRAFT" && (
        <p className="drawer-hint">Une fois envoyée, le magasinier est prévenu qu'une livraison arrive.</p>
      )}
      {canEdit && order.status === "SENT" && (
        <p className="drawer-hint">Confirmer : le fournisseur a accepté la commande.</p>
      )}

      {confirmCancel && (
        <ConfirmDialog
          title="Annuler cette commande ?"
          message={`La commande ${order.orderNumber} sera annulée. Elle ne pourra plus être réceptionnée ni facturée.`}
          confirmLabel="Oui, annuler"
          onConfirm={() => changeOrderStatus(order.id, "CANCELLED").then(onChanged)}
          onClose={() => setConfirmCancel(false)}
        />
      )}
    </DetailDrawer>
  );
}
