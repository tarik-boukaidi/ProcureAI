import { useEffect, useState } from "react";
import { FileText, Plus, Search, TriangleAlert } from "lucide-react";
import DemandeTable from "../components/DemandeTable";
import DemandeDrawer from "../components/DemandeDrawer";
import ConfirmDialog from "../../../shared/components/ConfirmDialog";
import { approveDemande, deleteDemande, getDemandes, rejectDemande } from "../services/demandeService";
import { getCurrentUser } from "../../auth/services/authService";
import "./DemandesPage.css";

const FILTERS = [
  { value: "ALL", label: "Toutes" },
  { value: "PENDING", label: "En attente" },
  { value: "APPROVED", label: "Approuvées" },
  { value: "REJECTED", label: "Rejetées" },
];

export default function DemandesPage() {
  const user = getCurrentUser();
  const canDecide = user.role === "MANAGER" || user.role === "ADMIN";

  const [demandes, setDemandes] = useState(null);
  const [error, setError] = useState("");
  const [actionError, setActionError] = useState("");
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState("ALL");
  const [creating, setCreating] = useState(false);
  const [busyId, setBusyId] = useState(null);
  // Action a confirmer dans le popup : { title, message, label, action } (null = popup ferme)
  const [confirm, setConfirm] = useState(null);

  function loadDemandes() {
    getDemandes(user.role)
      .then(setDemandes)
      .catch((err) => setError(err.message));
  }

  useEffect(() => {
    loadDemandes();
  }, []);

  async function handleApprove(demande) {
    setActionError("");
    setBusyId(demande.id);
    try {
      await approveDemande(demande.id);
      loadDemandes();
    } catch {
      setActionError(`La demande #${demande.id} n'a pas pu être approuvée. Elle a peut-être déjà été traitée.`);
    } finally {
      setBusyId(null);
    }
  }

  function askReject(demande) {
    setConfirm({
      title: "Rejeter cette demande ?",
      message: `La demande #${demande.id} sera rejetée et le demandeur sera notifié.`,
      label: "Rejeter",
      action: () => rejectDemande(demande.id),
    });
  }

  function askDelete(demande) {
    setConfirm({
      title: "Supprimer cette demande ?",
      message: `La demande #${demande.id} sera supprimée définitivement.`,
      label: "Supprimer",
      action: () => deleteDemande(demande.id),
    });
  }

  async function handleConfirm() {
    await confirm.action();
    loadDemandes();
  }

  function handleCreated() {
    setCreating(false);
    setFilter("ALL");
    loadDemandes();
  }

  if (error) return <p className="page-error">Impossible de charger les demandes. {error}</p>;
  if (!demandes) return <p className="page-loading">Chargement…</p>;

  const count = (value) => (value === "ALL" ? demandes.length : demandes.filter((d) => d.status === value).length);

  // Recherche dans le numero, les produits et le motif + filtre par statut. Plus recentes en premier.
  const visible = demandes
    .filter((d) => {
      const text = `#${d.id} ${d.items.map((item) => item.product).join(" ")} ${d.reason ?? ""}`.toLowerCase();
      const matchesSearch = text.includes(search.toLowerCase());
      const matchesFilter = filter === "ALL" || d.status === filter;
      return matchesSearch && matchesFilter;
    })
    .sort((a, b) => b.id - a.id);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Demandes d'achat</h1>
          <p className="page-subtitle">
            {canDecide ? "Approuvez ou rejetez les besoins d'achat de l'équipe." : "Vos besoins d'achat et leur validation."}
          </p>
        </div>
        <button className="btn btn-dark" onClick={() => setCreating(true)}>
          <Plus size={16} /> Nouvelle demande
        </button>
      </div>

      {actionError && (
        <div className="form-error" role="alert">
          <TriangleAlert size={16} /> {actionError}
        </div>
      )}

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
            <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Rechercher un produit, un motif, un n°…" />
          </label>
        </div>

        {demandes.length === 0 ? (
          <div className="empty-state">
            <FileText size={28} strokeWidth={1.5} />
            <h3>Aucune demande</h3>
            <p>Une demande d'achat est le point de départ : une fois approuvée, elle devient une commande.</p>
            <button className="btn btn-dark" onClick={() => setCreating(true)}>
              <Plus size={16} /> Nouvelle demande
            </button>
          </div>
        ) : visible.length === 0 ? (
          <p className="empty">Aucune demande ne correspond à votre recherche.</p>
        ) : (
          <DemandeTable
            demandes={visible}
            user={user}
            canDecide={canDecide}
            busyId={busyId}
            onApprove={handleApprove}
            onReject={askReject}
            onDelete={askDelete}
          />
        )}
      </div>

      {creating && <DemandeDrawer onClose={() => setCreating(false)} onSaved={handleCreated} />}

      {confirm && (
        <ConfirmDialog
          title={confirm.title}
          message={confirm.message}
          confirmLabel={confirm.label}
          onConfirm={handleConfirm}
          onClose={() => setConfirm(null)}
        />
      )}
    </div>
  );
}
