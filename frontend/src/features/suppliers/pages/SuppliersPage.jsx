import { useEffect, useState } from "react";
import { Building2, Plus, Search } from "lucide-react";
import SupplierTable from "../components/SupplierTable";
import SupplierDrawer from "../components/SupplierDrawer";
import ConfirmDialog from "../../../shared/components/ConfirmDialog";
import { deleteSupplier, getSuppliers } from "../services/supplierService";
import { getCurrentUser } from "../../auth/services/authService";

const FILTERS = [
  { value: "ALL", label: "Tous" },
  { value: "ACTIVE", label: "Actifs" },
  { value: "INACTIVE", label: "Inactifs" },
];

export default function SuppliersPage() {
  const canEdit = getCurrentUser().role === "ADMIN";
  const [suppliers, setSuppliers] = useState(null);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState("ALL");
  // null = panneau ferme, {} = nouveau fournisseur, un fournisseur = modification
  const [editing, setEditing] = useState(null);
  // Le fournisseur a supprimer (null = popup ferme)
  const [deleting, setDeleting] = useState(null);

  function loadSuppliers() {
    getSuppliers()
      .then(setSuppliers)
      .catch((err) => setError(err.message));
  }

  useEffect(() => {
    loadSuppliers();
  }, []);

  async function handleDelete() {
    await deleteSupplier(deleting.id);
    loadSuppliers();
  }

  function handleSaved() {
    setEditing(null);
    loadSuppliers();
  }

  if (error) return <p className="page-error">Impossible de charger les fournisseurs. {error}</p>;
  if (!suppliers) return <p className="page-loading">Chargement…</p>;

  const count = (value) => (value === "ALL" ? suppliers.length : suppliers.filter((s) => s.status === value).length);

  // Recherche dans le nom, l'email, la ville et le CIN + filtre par statut
  const visible = suppliers.filter((s) => {
    const text = `${s.name} ${s.email ?? ""} ${s.city ?? ""} ${s.cin}`.toLowerCase();
    const matchesSearch = text.includes(search.toLowerCase());
    const matchesFilter = filter === "ALL" || s.status === filter;
    return matchesSearch && matchesFilter;
  });

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Fournisseurs</h1>
          <p className="page-subtitle">Les entreprises auprès desquelles vous achetez.</p>
        </div>
        {canEdit && (
          <button className="btn btn-dark" onClick={() => setEditing({})}>
            <Plus size={16} /> Ajouter un fournisseur
          </button>
        )}
      </div>

      <div className="card table-card">
        <div className="toolbar">
          <div className="segmented">
            {FILTERS.map((f) => (
              <button key={f.value} className={filter === f.value ? "active" : ""} onClick={() => setFilter(f.value)}>
                {f.label} <span className="mono">{count(f.value)}</span>
              </button>
            ))}
          </div>
          <label className="search">
            <Search size={16} />
            <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Rechercher un nom, une ville, un CIN…" />
          </label>
        </div>

        {suppliers.length === 0 ? (
          <div className="empty-state">
            <Building2 size={28} strokeWidth={1.5} />
            <h3>Aucun fournisseur</h3>
            <p>Ajoutez votre premier fournisseur pour pouvoir créer des commandes.</p>
            {canEdit && (
              <button className="btn btn-dark" onClick={() => setEditing({})}>
                <Plus size={16} /> Ajouter un fournisseur
              </button>
            )}
          </div>
        ) : visible.length === 0 ? (
          <p className="empty">Aucun fournisseur ne correspond à votre recherche.</p>
        ) : (
          <SupplierTable suppliers={visible} canEdit={canEdit} onEdit={setEditing} onDelete={setDeleting} />
        )}
      </div>

      {editing && <SupplierDrawer supplier={editing} onClose={() => setEditing(null)} onSaved={handleSaved} />}

      {deleting && (
        <ConfirmDialog
          title="Supprimer ce fournisseur ?"
          message={`« ${deleting.name} » et tous ses produits seront supprimés définitivement.`}
          onConfirm={handleDelete}
          onClose={() => setDeleting(null)}
        />
      )}
    </div>
  );
}
