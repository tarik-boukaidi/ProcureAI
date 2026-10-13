import { Pencil, Trash2 } from "lucide-react";

const STATUS = {
  ACTIVE: { label: "Actif", tone: "green" },
  INACTIVE: { label: "Inactif", tone: "ink" },
};

export default function SupplierTable({ suppliers, canEdit, onEdit, onDelete }) {
  return (
    <table className="table">
      <thead>
        <tr>
          <th>Fournisseur</th>
          <th>CIN</th>
          <th>Téléphone</th>
          <th>Ville</th>
          <th>Statut</th>
          {canEdit && <th />}
        </tr>
      </thead>
      <tbody>
        {suppliers.map((supplier) => {
          const status = STATUS[supplier.status] ?? { label: supplier.status ?? "—", tone: "ink" };
          return (
            <tr key={supplier.id}>
              <td>
                <div className="identity-cell">
                  <span className="identity-initial">{supplier.name.charAt(0).toUpperCase()}</span>
                  <div>
                    <strong>{supplier.name}</strong>
                    <span>{supplier.email || "Pas d'email"}</span>
                  </div>
                </div>
              </td>
              <td className="mono">{supplier.cin}</td>
              <td className="muted">{supplier.phone || "—"}</td>
              <td className="muted">{supplier.city || "—"}</td>
              <td><span className={`tag tag-${status.tone}`}>{status.label}</span></td>
              {canEdit && (
                <td className="right">
                  <div className="row-actions">
                    <button className="icon-button" onClick={() => onEdit(supplier)} title="Modifier">
                      <Pencil size={15} />
                    </button>
                    <button className="icon-button danger" onClick={() => onDelete(supplier)} title="Supprimer">
                      <Trash2 size={15} />
                    </button>
                  </div>
                </td>
              )}
            </tr>
          );
        })}
      </tbody>
    </table>
  );
}
