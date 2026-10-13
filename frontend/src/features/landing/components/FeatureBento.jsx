import { Check } from "lucide-react";

// Chaque carte montre un petit morceau du produit, pas une icone decorative

function MatchVisual() {
  const columns = [
    { label: "Commande", value: "3 × 430,00" },
    { label: "Réception", value: "3 reçus" },
    { label: "Facture", value: "3 × 429,83" },
  ];
  return (
    <div className="visual-match">
      {columns.map((col) => (
        <div key={col.label} className="visual-match-col">
          <span>{col.label}</span>
          <strong className="mono">{col.value}</strong>
          <span className="visual-ok"><Check size={12} strokeWidth={3} /> Conforme</span>
        </div>
      ))}
    </div>
  );
}

function OcrVisual() {
  const fields = [
    ["N° facture", "FAC-2026-014"],
    ["ICE", "001234567000089"],
    ["Total TTC", "1 547,40"],
  ];
  return (
    <div className="visual-doc">
      <div className="visual-doc-lines">
        <span /><span /><span className="short" />
      </div>
      {fields.map(([label, value]) => (
        <div key={label} className="visual-doc-field">
          <span>{label}</span>
          <b className="mono">{value}</b>
        </div>
      ))}
    </div>
  );
}

function ApprovalVisual() {
  return (
    <div className="visual-approval">
      <div className="visual-approval-row">
        <span className="avatar">SA</span>
        <div>
          <b>Demande #12</b>
          <span>3 laptops · Salma A.</span>
        </div>
      </div>
      <div className="visual-approval-actions">
        <span className="pill-reject">Rejeter</span>
        <span className="pill-approve">Approuver</span>
      </div>
    </div>
  );
}

function JournalVisual() {
  const rows = [
    { account: "6111", label: "Achats de marchandises", debit: "1 547,40", credit: "" },
    { account: "4411", label: "Fournisseurs", debit: "", credit: "1 547,40" },
  ];
  return (
    <table className="visual-journal">
      <thead>
        <tr><th>Compte</th><th>Libellé</th><th className="right">Débit</th><th className="right">Crédit</th></tr>
      </thead>
      <tbody>
        {rows.map((row) => (
          <tr key={row.account}>
            <td className="mono">{row.account}</td>
            <td>{row.label}</td>
            <td className="mono right">{row.debit}</td>
            <td className="mono right">{row.credit}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

const CARDS = [
  { title: "Rapprochement 3 voies", text: "Quantités et prix comparés ligne par ligne, avec une tolérance que vous fixez.", visual: <MatchVisual />, wide: true },
  { title: "Lecture des factures", text: "PDF ou photo : numéro, ICE et montants extraits par OCR.", visual: <OcrVisual /> },
  { title: "Approbations", text: "Le manager valide en un clic, le demandeur est prévenu.", visual: <ApprovalVisual /> },
  { title: "Comptabilité CGNC", text: "Facture validée, paiement effectué : l'écriture est passée, en partie double.", visual: <JournalVisual />, wide: true },
];

export default function FeatureBento() {
  return (
    <section className="section section-white">
      <div className="container">
        <div className="section-head">
          <span className="eyebrow">Le produit</span>
          <h2 className="title">Un seul outil, <em>du besoin à l'écriture.</em></h2>
        </div>
        <div className="bento">
          {CARDS.map((card) => (
            <article key={card.title} className={card.wide ? "bento-card wide" : "bento-card"}>
              <div className="bento-visual">{card.visual}</div>
              <h3>{card.title}</h3>
              <p>{card.text}</p>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
