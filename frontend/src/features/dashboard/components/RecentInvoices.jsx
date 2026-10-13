import { Link } from "react-router-dom";
import { formatDate, formatMoney } from "../../../shared/utils/format";
import { INVOICE_STATUS, statusOf } from "../../../shared/constants/status";

export default function RecentInvoices({ invoices }) {
  return (
    <div className="card table-card">
      <div className="card-head">
        <div>
          <h2>Factures récentes</h2>
          <span className="card-subtitle">Les 6 dernières factures reçues</span>
        </div>
        <Link to="/factures" className="card-link">Tout voir</Link>
      </div>

      {invoices.length === 0 ? (
        <p className="empty">Aucune facture pour le moment.</p>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>N°</th>
              <th>Fournisseur</th>
              <th>Échéance</th>
              <th className="right">Montant TTC (MAD)</th>
              <th>Statut</th>
            </tr>
          </thead>
          <tbody>
            {invoices.map((invoice) => {
              const status = statusOf(INVOICE_STATUS, invoice.status);
              return (
                <tr key={invoice.id}>
                  <td className="mono">{invoice.invoiceNumber}</td>
                  <td>{invoice.supplierName}</td>
                  <td className="muted">{formatDate(invoice.dueDate)}</td>
                  <td className="mono right">{formatMoney(invoice.totalTtc)}</td>
                  <td><span className={`tag tag-${status.tone}`}>{status.label}</span></td>
                </tr>
              );
            })}
          </tbody>
        </table>
      )}
    </div>
  );
}
