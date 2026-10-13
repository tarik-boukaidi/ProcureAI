import { useEffect, useState } from "react";
import { CircleCheck, CircleX, Clock, Inbox } from "lucide-react";
import StatCard from "../../../shared/components/StatCard";
import { loadDemandes } from "../services/dashboardService";

const STATUS = {
  PENDING: { label: "En attente", tone: "amber" },
  APPROVED: { label: "Approuvée", tone: "green" },
  REJECTED: { label: "Rejetée", tone: "red" },
};

// Tableau de bord EMPLOYEE (ses demandes) et MANAGER (toutes les demandes)
export default function DemandesDashboard({ role }) {
  const [demandes, setDemandes] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    loadDemandes(role)
      .then(setDemandes)
      .catch((err) => setError(err.message));
  }, [role]);

  if (error) return <p className="page-error">Impossible de charger les demandes. {error}</p>;
  if (!demandes) return <p className="page-loading">Chargement…</p>;

  const count = (status) => demandes.filter((d) => d.status === status).length;
  const latest = [...demandes].sort((a, b) => b.id - a.id).slice(0, 8);

  return (
    <>
      <div className="stats">
        <StatCard icon={Inbox} label="Demandes" value={demandes.length} hint={role === "MANAGER" ? "Toutes les demandes" : "Vos demandes"} />
        <StatCard icon={Clock} label="En attente" value={count("PENDING")} hint={role === "MANAGER" ? "À approuver" : "En cours de validation"} />
        <StatCard icon={CircleCheck} label="Approuvées" value={count("APPROVED")} />
        <StatCard icon={CircleX} label="Rejetées" value={count("REJECTED")} />
      </div>

      <div className="card table-card">
        <div className="card-head">
          <div>
            <h2>{role === "MANAGER" ? "Dernières demandes" : "Mes dernières demandes"}</h2>
            <span className="card-subtitle">Les 8 plus récentes</span>
          </div>
        </div>

        {latest.length === 0 ? (
          <p className="empty">Aucune demande pour le moment.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>N°</th>
                <th>Produits</th>
                <th>Motif</th>
                <th>Statut</th>
              </tr>
            </thead>
            <tbody>
              {latest.map((demande) => {
                const status = STATUS[demande.status];
                return (
                  <tr key={demande.id}>
                    <td className="mono">#{demande.id}</td>
                    <td>{demande.items.map((item) => `${item.product} × ${item.quantity}`).join(", ")}</td>
                    <td className="muted">{demande.reason}</td>
                    <td><span className={`tag tag-${status.tone}`}>{status.label}</span></td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>
    </>
  );
}
