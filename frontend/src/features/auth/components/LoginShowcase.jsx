import { Check } from "lucide-react";

// Panneau de droite : un apercu du produit, pas une illustration decorative

const TO_PAY = [
  { supplier: "Atlas Informatique", amount: "1 547,40", due: "Demain" },
  { supplier: "Souss Logistique", amount: "4 120,80", due: "Jeu." },
  { supplier: "Rif Emballages", amount: "12 300,00", due: "Ven." },
];

const MATCH = ["Commande PO-0412", "Réception REC-0388", "Facture FAC-2026-014"];

export default function LoginShowcase() {
  return (
    <aside className="showcase">
      <div className="showcase-cards">
        <div className="showcase-card">
          <span className="showcase-label">À payer cette semaine</span>
          <strong className="showcase-total mono">17 968,20 <small>MAD</small></strong>
          <ul className="showcase-list">
            {TO_PAY.map((row) => (
              <li key={row.supplier}>
                <span>{row.supplier}</span>
                <span className="mono">{row.amount}</span>
                <span className="showcase-due">{row.due}</span>
              </li>
            ))}
          </ul>
        </div>

        <div className="showcase-card showcase-card-small">
          <span className="showcase-label">Rapprochement 3 voies</span>
          <ul className="showcase-match">
            {MATCH.map((item) => (
              <li key={item}>
                <span className="showcase-check"><Check size={11} strokeWidth={3} /></span>
                <span className="mono">{item}</span>
              </li>
            ))}
          </ul>
          <span className="showcase-ok">Conforme · prêt à payer</span>
        </div>
      </div>

      <div className="showcase-text">
        <h2>Chaque dirham dépensé, <em>justifié.</em></h2>
        <p className="mono">Rapprochement 3 voies · OCR des factures · Plan comptable CGNC</p>
      </div>
    </aside>
  );
}
