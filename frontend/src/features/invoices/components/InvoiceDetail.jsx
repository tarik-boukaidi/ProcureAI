import DetailDrawer from "../../../shared/components/DetailDrawer";
import { INVOICE_STATUS, statusOf } from "../../../shared/constants/status";
import { formatFullDate, formatMoney } from "../../../shared/utils/format";

export default function InvoiceDetail({ invoice, onClose }) {
  const status = statusOf(INVOICE_STATUS, invoice.status);

  return (
    <DetailDrawer title={`Facture ${invoice.invoiceNumber}`} subtitle={invoice.supplierName} onClose={onClose}>
      <dl className="detail-list">
        <dt>Statut</dt>
        <dd><span className={`tag tag-${status.tone}`}>{status.label}</span></dd>
        <dt>Commande</dt>
        <dd className="mono">{invoice.orderNumber}</dd>
        <dt>Date de facture</dt>
        <dd>{formatFullDate(invoice.invoiceDate)}</dd>
        <dt>Échéance</dt>
        <dd>{formatFullDate(invoice.dueDate)}</dd>
      </dl>

      <h3 className="detail-section">Lignes facturées</h3>
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
          {invoice.lines.map((line) => (
            <tr key={line.id}>
              <td>{line.productName || `Produit #${line.productId}`}</td>
              <td className="mono right">{line.quantity}</td>
              <td className="mono right">{formatMoney(line.unitPrice)}</td>
              <td className="mono right">{formatMoney(line.unitPrice * line.quantity)}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="detail-subtotal">
        <span>Total HT</span>
        <span className="mono">{formatMoney(invoice.totalAmount)} MAD</span>
      </div>
      <div className="detail-subtotal">
        <span>TVA {Number(invoice.vatRate)} %</span>
        <span className="mono">{formatMoney(invoice.vatAmount)} MAD</span>
      </div>
      <div className="detail-total">
        <span>Total TTC</span>
        <span className="mono">{formatMoney(invoice.totalTtc)} MAD</span>
      </div>
    </DetailDrawer>
  );
}
