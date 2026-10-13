import { useEffect, useState } from "react";
import { Plus, Search, Users } from "lucide-react";
import UserTable from "../components/UserTable";
import UserDrawer from "../components/UserDrawer";
import ConfirmDialog from "../../../shared/components/ConfirmDialog";
import StatusFilter, { applyFilter } from "../../../shared/components/StatusFilter";
import { deleteUser, getUsers, manageableRoles } from "../services/userService";
import { getCurrentUser } from "../../auth/services/authService";

const FILTERS = [
  { value: "ALL", label: "Tous" },
  { value: "MANAGER", label: "Managers", test: (u) => u.role === "MANAGER" },
  { value: "COMPTABLE", label: "Comptables", test: (u) => u.role === "COMPTABLE" },
  { value: "MAGASINIER", label: "Magasiniers", test: (u) => u.role === "MAGASINIER" },
  { value: "EMPLOYEE", label: "Employés", test: (u) => u.role === "EMPLOYEE" },
];

export default function UsersPage() {
  const roles = manageableRoles(getCurrentUser().role);
  const isAdmin = roles.length > 1;

  const [users, setUsers] = useState(null);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState("ALL");
  // null = panneau ferme, {} = nouveau compte, un utilisateur = modification
  const [editing, setEditing] = useState(null);
  // Le compte a supprimer (null = popup ferme)
  const [deleting, setDeleting] = useState(null);

  function loadUsers() {
    getUsers()
      .then(setUsers)
      .catch((err) => setError(err.message));
  }

  useEffect(() => {
    loadUsers();
  }, []);

  async function handleDelete() {
    await deleteUser(deleting.id);
    loadUsers();
  }

  function handleSaved() {
    setEditing(null);
    loadUsers();
  }

  if (error) return <p className="page-error">Impossible de charger les utilisateurs. {error}</p>;
  if (!users) return <p className="page-loading">Chargement…</p>;

  // Recherche dans le nom, l'email et le telephone + filtre par role
  const visible = applyFilter(FILTERS, filter, users).filter((u) =>
    `${u.name ?? ""} ${u.email} ${u.phone ?? ""}`.toLowerCase().includes(search.toLowerCase())
  );

  const addButton = (
    <button className="btn btn-dark" onClick={() => setEditing({})}>
      <Plus size={16} /> {isAdmin ? "Nouvel utilisateur" : "Nouvel employé"}
    </button>
  );

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Utilisateurs</h1>
          <p className="page-subtitle">
            {isAdmin ? "Créez et gérez les comptes de toute l'équipe." : "Créez et gérez les comptes de votre équipe."}
          </p>
        </div>
        {addButton}
      </div>

      <div className="card table-card">
        <div className="toolbar">
          {isAdmin ? <StatusFilter filters={FILTERS} items={users} value={filter} onChange={setFilter} /> : <span />}
          <label className="search">
            <Search size={16} />
            <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Rechercher un nom, un email…" />
          </label>
        </div>

        {users.length === 0 ? (
          <div className="empty-state">
            <Users size={28} strokeWidth={1.5} />
            <h3>Aucun compte</h3>
            <p>Créez les comptes de votre équipe pour qu'ils puissent faire leurs demandes d'achat.</p>
            {addButton}
          </div>
        ) : visible.length === 0 ? (
          <p className="empty">Aucun utilisateur ne correspond à votre recherche.</p>
        ) : (
          <UserTable users={visible} onEdit={setEditing} onDelete={setDeleting} />
        )}
      </div>

      {editing && <UserDrawer user={editing} roles={roles} onClose={() => setEditing(null)} onSaved={handleSaved} />}

      {deleting && (
        <ConfirmDialog
          title="Supprimer ce compte ?"
          message={`« ${deleting.name || deleting.email} » ne pourra plus se connecter. Ses demandes restent dans l'historique.`}
          onConfirm={handleDelete}
          onClose={() => setDeleting(null)}
        />
      )}
    </div>
  );
}
