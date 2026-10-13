import { useState } from "react";
import { Check } from "lucide-react";

const ROLES = [
  {
    id: "employee",
    name: "Employé",
    headline: "Demander sans relancer.",
    text: "Il crée sa demande en trente secondes et voit à tout moment où elle en est.",
    points: ["Crée et suit ses demandes", "Peut supprimer une demande encore en attente", "Est notifié dès qu'elle est approuvée ou rejetée"],
  },
  {
    id: "manager",
    name: "Manager",
    headline: "Décider vite, avec le contexte.",
    text: "Il reçoit les nouvelles demandes de son équipe et les traite en un clic.",
    points: ["Approuve ou rejette les demandes", "Est notifié de chaque nouvelle demande", "Garde la maîtrise des dépenses"],
  },
  {
    id: "admin",
    name: "Finance",
    headline: "Payer juste, et une seule fois.",
    text: "L'équipe finance contrôle les factures, les paie et suit les dettes fournisseurs.",
    points: ["Lance le rapprochement et lit les factures par OCR", "Paie les factures validées, triées par échéance", "Consulte le journal, les soldes et le rapport mensuel"],
  },
];

export default function RolesTabs() {
  const [activeId, setActiveId] = useState("employee");
  const role = ROLES.find((r) => r.id === activeId);

  return (
    <section className="section section-white" id="equipes">
      <div className="container">
        <div className="section-head">
          <span className="eyebrow">Pour chaque équipe</span>
          <h2 className="title">Chacun voit <em>ce qui le concerne.</em></h2>
        </div>

        <div className="tabs">
          {ROLES.map((r) => (
            <button
              key={r.id}
              className={r.id === activeId ? "tab active" : "tab"}
              onClick={() => setActiveId(r.id)}
            >
              {r.name}
            </button>
          ))}
        </div>

        <div className="role">
          <div>
            <h3 className="role-headline">{role.headline}</h3>
            <p className="role-text">{role.text}</p>
          </div>
          <ul className="role-points">
            {role.points.map((point) => (
              <li key={point}><Check size={16} /> {point}</li>
            ))}
          </ul>
        </div>
      </div>
    </section>
  );
}
