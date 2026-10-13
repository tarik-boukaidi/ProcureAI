import { CircleCheck, TriangleAlert } from "lucide-react";
import DetailDrawer from "../../../shared/components/DetailDrawer";
import { formatDateTime, formatMoney } from "../../../shared/utils/format";

// Type d'ecart -> titre + phrase lisible (expected / actual viennent de matching-service)
const DISCREPANCY = {
  SUPPLIER_MISMATCH: {
    title: "Mauvais fournisseur",
    text: (d) => `La commande est chez le fournisseur #${d.expectedValue}, la facture vient du fournisseur #${d.actualValue}.`,
  },
  PRODUCT_NOT_ORDERED: {
    title: "Produit non commandé",
    text: (d) => `${d.actualValue} unité(s) facturée(s) alors que ce produit n'est pas dans la commande.`,
  },
  PRICE_MISMATCH: {
    title: "Prix différent",
    text: (d) => `Prix commandé ${formatMoney(d.expectedValue)} MAD, prix facturé ${formatMoney(d.actualValue)} MAD.`,
  },
  QUANTITY_EXCEEDS_RECEIVED: {
    title: "Quantité non reçue",
    text: (d) => `${d.actualValue} unité(s) facturée(s), mais seulement ${d.expectedValue} reçue(s) et pas encore facturée(s).`,
  },
};

export default function MatchDetail({ result, onClose }) {
  const invoice = result.invoice;
  // Nom du produit concerne par l'ecart, trouve dans les lignes de la facture
  const productName = (productId) =>
    invoice?.lines.find((line) => line.productId === productId)?.productName ?? `Produit #${productId}`;

  return (
    <DetailDrawer
      title={`Rapprochement ${invoice ? invoice.invoiceNumber : `#${result.invoiceId}`}`}
      subtitle={`Vérifié le ${formatDateTime(result.checkedAt)}`}
      onClose={onClose}
    >
      <dl className="detail-list">
        <dt>Fournisseur</dt>
        <dd>{invoice?.supplierName ?? "—"}</dd>
        <dt>Commande</dt>
        <dd className="mono">{invoice?.orderNumber ?? `#${result.purchaseOrderId}`}</dd>
        <dt>Montant facturé HT</dt>
        <dd className="mono">{invoice ? `${formatMoney(invoice.totalAmount)} MAD` : "—"}</dd>
      </dl>

      {result.discrepancies.length === 0 ? (
        <div className="form-success">
          <CircleCheck size={16} /> Facture, commande et réception concordent. La facture a été validée.
        </div>
      ) : (
        <>
          <h3 className="detail-section">{result.discrepancies.length} écart(s) détecté(s)</h3>
          <ul className="discrepancies">
            {result.discrepancies.map((d) => {
              const info = DISCREPANCY[d.type] ?? { title: d.type, text: () => "" };
              return (
                <li key={d.id}>
                  <TriangleAlert size={16} />
                  <div>
                    <strong>{info.title}{d.productId && ` — ${productName(d.productId)}`}</strong>
                    <p>{info.text(d)}</p>
                  </div>
                </li>
              );
            })}
          </ul>
        </>
      )}
    </DetailDrawer>
  );
}
