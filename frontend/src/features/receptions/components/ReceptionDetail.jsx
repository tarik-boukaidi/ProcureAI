import DetailDrawer from "../../../shared/components/DetailDrawer";
import { RECEPTION_STATE } from "../services/receptionService";
import { formatFullDate } from "../../../shared/utils/format";

export default function ReceptionDetail({ reception, onClose }) {
  const state = RECEPTION_STATE[reception.state];

  return (
    <DetailDrawer title={`Réception #${reception.id}`} subtitle={reception.supplierName} onClose={onClose}>
      <dl className="detail-list">
        <dt>État</dt>
        <dd><span className={`tag tag-${state.tone}`}>{state.label}</span></dd>
        <dt>Commande</dt>
        <dd className="mono">{reception.orderNumber}</dd>
        <dt>Bon de livraison</dt>
        <dd className="mono">{reception.deliveryNoteNumber || "—"}</dd>
        <dt>Reçue le</dt>
        <dd>{formatFullDate(reception.receivedDate)}</dd>
        <dt>Commentaire</dt>
        <dd>{reception.comment || "—"}</dd>
      </dl>

      <h3 className="detail-section">Articles reçus</h3>
      <table className="table">
        <thead>
          <tr>
            <th>Produit</th>
            <th className="right">Commandé</th>
            <th className="right">Reçu</th>
            <th className="right">Refusé</th>
          </tr>
        </thead>
        <tbody>
          {reception.lines.map((line) => (
            <tr key={line.id}>
              <td>{line.productName || `Produit #${line.productId}`}</td>
              <td className="mono right">{line.quantityOrdered}</td>
              <td className="mono right">{line.quantityReceived}</td>
              <td className={line.quantityRejected > 0 ? "mono right text-red" : "mono right muted"}>{line.quantityRejected}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </DetailDrawer>
  );
}
