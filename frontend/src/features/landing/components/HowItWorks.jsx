const PHASES = [
  {
    number: "01",
    title: "Demander",
    text: "L'employé décrit son besoin. Son manager l'approuve ou la rejette, avec une trace de chaque décision.",
    steps: ["Demande d'achat", "Approbation"],
  },
  {
    number: "02",
    title: "Acheter et recevoir",
    text: "La demande approuvée devient un bon de commande. À la livraison, on note ce qui est reçu et ce qui est rejeté.",
    steps: ["Bon de commande", "Réception"],
  },
  {
    number: "03",
    title: "Contrôler et payer",
    text: "La facture est lue, comparée à la commande et à la réception, puis payée. L'écriture comptable suit.",
    steps: ["Facture", "Rapprochement", "Paiement", "Comptabilité"],
  },
];

export default function HowItWorks() {
  return (
    <section className="section" id="fonctionnement">
      <div className="container">
        <div className="section-head">
          <span className="eyebrow">Fonctionnement</span>
          <h2 className="title">Trois temps, <em>aucune étape sautée.</em></h2>
          <p className="lead">Une facture ne peut pas être payée tant qu'elle n'a pas été rapprochée de sa commande et de sa réception.</p>
        </div>
        <div className="phases">
          {PHASES.map((phase) => (
            <article key={phase.number} className="phase">
              <span className="phase-number mono">{phase.number}</span>
              <h3>{phase.title}</h3>
              <p>{phase.text}</p>
              <ul>
                {phase.steps.map((step) => <li key={step}>{step}</li>)}
              </ul>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
