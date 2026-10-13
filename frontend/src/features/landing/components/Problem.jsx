import { Check, X } from "lucide-react";

const BEFORE = [
  "Les demandes arrivent par e-mail, WhatsApp ou de vive voix",
  "Les factures sont ressaisies à la main dans un tableur",
  "Personne ne vérifie que la quantité facturée a bien été reçue",
  "Le comptable reconstitue les écritures en fin de mois",
];

const AFTER = [
  "Chaque demande a un statut, un demandeur et un approbateur",
  "La facture est lue automatiquement, vous ne faites que vérifier",
  "Commande, réception et facture sont comparées avant tout paiement",
  "Les écritures 6111, 4411 et 5141 sont passées au fil de l'eau",
];

export default function Problem() {
  return (
    <section className="section">
      <div className="container problem">
        <div>
          <span className="eyebrow">Le constat</span>
          <h2 className="title">
            Aujourd'hui, un achat, c'est un e-mail, un tableur et <em>beaucoup de relances.</em>
          </h2>
        </div>

        <div className="compare">
          <div className="compare-col">
            <span className="compare-label">Sans ProcureAI</span>
            <ul>
              {BEFORE.map((item) => (
                <li key={item}><X size={16} className="icon-x" /> {item}</li>
              ))}
            </ul>
          </div>
          <div className="compare-col compare-after">
            <span className="compare-label">Avec ProcureAI</span>
            <ul>
              {AFTER.map((item) => (
                <li key={item}><Check size={16} className="icon-check" /> {item}</li>
              ))}
            </ul>
          </div>
        </div>
      </div>
    </section>
  );
}
