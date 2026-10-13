import { Check, Trash2, X } from "lucide-react";

const STATUS = {
  PENDING: { label: "En attente", tone: "amber" },
  APPROVED: { label: "Approuvée", tone: "green" },
  REJECTED: { label: "Rejetée", tone: "red" },
};

export default function DemandeTable({ demandes, user, canDecide, busyId, onApprove, onReject, onDelete }) {
  return (
    <table className="table">
      <thead>
        <tr>
          <th>N°</th>
          <th>Article</th>
          <th>Motif</th>
          {canDecide && <th>Demandeur</th>}
          <th>Statut</th>
          <th />
        </tr>
      </thead>
      <tbody>
        {demandes.map((demande) => {
          const status = STATUS[demande.status];
          const isPending = demande.status === "PENDING";
          const isMine = demande.requesterId === user.id;
          const busy = busyId === demande.id;

          return (
            <tr key={demande.id}>
              <td className="mono">#{demande.id}</td>
              <td>
                {demande.items.map((item) => (
                  <div key={item.id} className="demande-item">
                    <strong>{item.product}</strong>
                    <span className="mono">× {item.quantity}</span>
                  </div>
                ))}
              </td>
              <td className="muted demande-reason">{demande.reason || "—"}</td>
              {canDecide && <td className="muted">{isMine ? "Vous" : `Utilisateur #${demande.requesterId}`}</td>}
              <td><span className={`tag tag-${status.tone}`}>{status.label}</span></td>
              <td className="right">
                {isPending && (
                  <div className="row-actions">
                    {canDecide && (
                      <>
                        <button className="btn btn-sm btn-dark" onClick={() => onApprove(demande)} disabled={busy}>
                          <Check size={14} /> Approuver
                        </button>
                        <button className="btn btn-sm btn-outline" onClick={() => onReject(demande)} disabled={busy}>
                          <X size={14} /> Rejeter
                        </button>
                      </>
                    )}
                    {isMine && (
                      <button className="icon-button danger" onClick={() => onDelete(demande)} title="Supprimer">
                        <Trash2 size={15} />
                      </button>
                    )}
                  </div>
                )}
              </td>
            </tr>
          );
        })}
      </tbody>
    </table>
  );
}
