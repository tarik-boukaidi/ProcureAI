import { Pencil, Trash2 } from "lucide-react";
import { USER_ROLE, statusOf } from "../../../shared/constants/status";

export default function UserTable({ users, onEdit, onDelete }) {
  return (
    <table className="table">
      <thead>
        <tr>
          <th>Utilisateur</th>
          <th>Rôle</th>
          <th>Téléphone</th>
          <th>Adresse</th>
          <th />
        </tr>
      </thead>
      <tbody>
        {users.map((user) => {
          const role = statusOf(USER_ROLE, user.role);
          const name = user.name || user.email;
          return (
            <tr key={user.id}>
              <td>
                <div className="identity-cell">
                  <span className="identity-initial">{name.charAt(0).toUpperCase()}</span>
                  <div>
                    <strong>{name}</strong>
                    <span>{user.email}</span>
                  </div>
                </div>
              </td>
              <td><span className={`tag tag-${role.tone}`}>{role.label}</span></td>
              <td className="muted">{user.phone || "—"}</td>
              <td className="muted">{user.address || "—"}</td>
              <td className="right">
                <div className="row-actions">
                  <button className="icon-button" onClick={() => onEdit(user)} title="Modifier">
                    <Pencil size={15} />
                  </button>
                  <button className="icon-button danger" onClick={() => onDelete(user)} title="Supprimer">
                    <Trash2 size={15} />
                  </button>
                </div>
              </td>
            </tr>
          );
        })}
      </tbody>
    </table>
  );
}
