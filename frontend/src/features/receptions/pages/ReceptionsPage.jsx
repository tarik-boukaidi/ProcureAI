import { useEffect, useState } from "react";
import { PackageCheck, Plus, Search } from "lucide-react";
import ReceptionDetail from "../components/ReceptionDetail";
import ReceptionDrawer from "../components/ReceptionDrawer";
import StatusFilter, { applyFilter } from "../../../shared/components/StatusFilter";
import { loadReceptions, RECEPTION_STATE } from "../services/receptionService";
import { getCurrentUser } from "../../auth/services/authService";
import { formatDate } from "../../../shared/utils/format";
import "./ReceptionsPage.css";

const FILTERS = [
  { value: "ALL", label: "Toutes" },
  { value: "COMPLETE", label: "Complètes", test: (r) => r.state === "COMPLETE" },
  { value: "PARTIAL", label: "Partielles", test: (r) => r.state === "PARTIAL" },
  { value: "REJECTED", label: "Avec refus", test: (r) => r.state === "REJECTED" },
];

// Total d'une colonne sur toutes les lignes de la reception
const sum = (lines, field) => lines.reduce((total, line) => total + line[field], 0);

export default function ReceptionsPage() {
  const canReceive = ["MAGASINIER", "ADMIN"].includes(getCurrentUser().role);
  const [receptions, setReceptions] = useState(null);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState("ALL");
  const [selected, setSelected] = useState(null);
  const [creating, setCreating] = useState(false);

  function refresh() {
    loadReceptions()
      .then(setReceptions)
      .catch((err) => setError(err.message));
  }

  useEffect(() => {
    refresh();
  }, []);

  function handleSaved() {
    setCreating(false);
    setFilter("ALL");
    refresh();
  }

  if (error) return <p className="page-error">Impossible de charger les réceptions. {error}</p>;
  if (!receptions) return <p className="page-loading">Chargement…</p>;

  const visible = applyFilter(FILTERS, filter, receptions)
    .filter((r) =>
      `${r.orderNumber} ${r.supplierName} ${r.deliveryNoteNumber ?? ""}`.toLowerCase().includes(search.toLowerCase())
    )
    .sort((a, b) => b.id - a.id);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Réceptions</h1>
          <p className="page-subtitle">Ce qui a été réellement livré, commande par commande.</p>
        </div>
        {canReceive && (
          <button className="btn btn-dark" onClick={() => setCreating(true)}>
            <Plus size={16} /> Nouvelle réception
          </button>
        )}
      </div>

      <div className="card table-card">
        <div className="toolbar">
          <StatusFilter filters={FILTERS} items={receptions} value={filter} onChange={setFilter} />
          <label className="search">
            <Search size={16} />
            <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Rechercher une commande, un BL…" />
          </label>
        </div>

        {receptions.length === 0 ? (
          <div className="empty-state">
            <PackageCheck size={28} strokeWidth={1.5} />
            <h3>Aucune réception</h3>
            <p>Les livraisons reçues apparaîtront ici, avec les quantités acceptées et refusées.</p>
          </div>
        ) : visible.length === 0 ? (
          <p className="empty">Aucune réception ne correspond à votre recherche.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>N°</th>
                <th>Commande</th>
                <th>Fournisseur</th>
                <th>Bon de livraison</th>
                <th>Reçue le</th>
                <th className="right">Reçu / commandé</th>
                <th>État</th>
              </tr>
            </thead>
            <tbody>
              {visible.map((reception) => {
                const state = RECEPTION_STATE[reception.state];
                return (
                  <tr key={reception.id} className="clickable" onClick={() => setSelected(reception)}>
                    <td className="mono">#{reception.id}</td>
                    <td className="mono">{reception.orderNumber}</td>
                    <td>{reception.supplierName}</td>
                    <td className="mono muted">{reception.deliveryNoteNumber || "—"}</td>
                    <td className="muted">{formatDate(reception.receivedDate)}</td>
                    <td className="mono right">
                      {sum(reception.lines, "quantityReceived")} / {sum(reception.lines, "quantityOrdered")}
                    </td>
                    <td><span className={`tag tag-${state.tone}`}>{state.label}</span></td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>

      {selected && <ReceptionDetail reception={selected} onClose={() => setSelected(null)} />}
      {creating && <ReceptionDrawer onClose={() => setCreating(false)} onSaved={handleSaved} />}
    </div>
  );
}
