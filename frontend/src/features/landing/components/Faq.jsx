const QUESTIONS = [
  {
    question: "Faut-il saisir les factures à la main ?",
    answer: "Non. Vous déposez le PDF ou la photo, l'OCR remplit le numéro, la date, l'ICE et les montants. Vous vérifiez, puis vous validez.",
  },
  {
    question: "Que se passe-t-il si le prix facturé ne correspond pas à la commande ?",
    answer: "Le rapprochement signale l'écart. S'il dépasse la tolérance que vous avez fixée, la facture est rejetée et l'équipe finance est prévenue.",
  },
  {
    question: "Une facture peut-elle être payée deux fois ?",
    answer: "Non. Seules les factures validées sont payables, et une facture déjà payée ne peut pas l'être à nouveau.",
  },
  {
    question: "Est-ce adapté au plan comptable marocain ?",
    answer: "Oui. Les écritures utilisent les comptes du CGNC : 6111 pour les achats, 4411 pour les fournisseurs, 5141 pour la banque.",
  },
  {
    question: "Qui peut approuver une demande d'achat ?",
    answer: "Les managers et les administrateurs. Un employé ne peut ni approuver ni payer, il suit simplement ses propres demandes.",
  },
];

export default function Faq() {
  return (
    <section className="section" id="faq">
      <div className="container faq">
        <div>
          <span className="eyebrow">FAQ</span>
          <h2 className="title">Questions <em>fréquentes.</em></h2>
        </div>
        <div className="faq-list">
          {QUESTIONS.map((item) => (
            <details key={item.question} className="faq-item">
              <summary>{item.question}</summary>
              <p>{item.answer}</p>
            </details>
          ))}
        </div>
      </div>
    </section>
  );
}
