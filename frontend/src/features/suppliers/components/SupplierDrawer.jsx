import { useState } from "react";
import { TriangleAlert, X } from "lucide-react";
import { createSupplier, updateSupplier } from "../services/supplierService";

// Code HTTP renvoye par supplier-service -> message clair pour l'utilisateur
function errorMessage(status) {
  if (status === 409) return "Ce CIN est déjà utilisé par un autre fournisseur.";
  if (status === 400) return "Vérifiez les champs : nom et CIN obligatoires, email valide, ICE de 15 chiffres.";
  return "Enregistrement impossible. Réessayez dans un instant.";
}

// Panneau a droite pour ajouter (supplier sans id) ou modifier un fournisseur
export default function SupplierDrawer({ supplier, onClose, onSaved }) {
  const isNew = !supplier.id;
  const [form, setForm] = useState({
    name: supplier.name ?? "",
    cin: supplier.cin ?? "",
    ice: supplier.ice ?? "",
    email: supplier.email ?? "",
    phone: supplier.phone ?? "",
    address: supplier.address ?? "",
    city: supplier.city ?? "",
    status: supplier.status ?? "ACTIVE",
  });
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  function update(field, value) {
    setForm({ ...form, [field]: value });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setSaving(true);
    try {
      if (isNew) {
        await createSupplier(form);
      } else {
        await updateSupplier(supplier.id, form);
      }
      onSaved();
    } catch (err) {
      setError(errorMessage(err.status));
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="drawer-overlay" onClick={onClose}>
      {/* stopPropagation : un clic dans le panneau ne doit pas le fermer */}
      <form className="drawer" onClick={(e) => e.stopPropagation()} onSubmit={handleSubmit}>
        <header className="drawer-header">
          <h2>{isNew ? "Nouveau fournisseur" : "Modifier le fournisseur"}</h2>
          <button type="button" className="icon-button" onClick={onClose} title="Fermer">
            <X size={18} />
          </button>
        </header>

        <div className="drawer-body">
          {error && (
            <div className="form-error" role="alert">
              <TriangleAlert size={16} /> {error}
            </div>
          )}

          <label className="field">
            <span>Nom *</span>
            <input value={form.name} onChange={(e) => update("name", e.target.value)} placeholder="Atlas Informatique" required autoFocus />
          </label>

          <div className="form-row">
            <label className="field">
              <span>CIN *</span>
              <input value={form.cin} onChange={(e) => update("cin", e.target.value)} placeholder="BK123456" required />
            </label>
            <label className="field">
              <span>Statut</span>
              <select value={form.status} onChange={(e) => update("status", e.target.value)}>
                <option value="ACTIVE">Actif</option>
                <option value="INACTIVE">Inactif</option>
              </select>
            </label>
          </div>

          <label className="field">
            <span>ICE</span>
            <input
              value={form.ice}
              onChange={(e) => update("ice", e.target.value.trim())}
              placeholder="001525874000062"
              inputMode="numeric"
              pattern="\d{15}"
              title="15 chiffres"
              className="mono"
            />
          </label>

          <div className="form-row">
            <label className="field">
              <span>Email</span>
              <input type="email" value={form.email} onChange={(e) => update("email", e.target.value)} placeholder="contact@atlas.ma" />
            </label>
            <label className="field">
              <span>Téléphone</span>
              <input value={form.phone} onChange={(e) => update("phone", e.target.value)} placeholder="0522 00 00 00" />
            </label>
          </div>

          <label className="field">
            <span>Adresse</span>
            <input value={form.address} onChange={(e) => update("address", e.target.value)} placeholder="12 rue Allal Ben Abdellah" />
          </label>

          <label className="field">
            <span>Ville</span>
            <input value={form.city} onChange={(e) => update("city", e.target.value)} placeholder="Casablanca" />
          </label>
        </div>

        <footer className="drawer-footer">
          <button type="button" className="btn btn-outline" onClick={onClose}>Annuler</button>
          <button type="submit" className="btn btn-dark" disabled={saving}>
            {saving ? "Enregistrement…" : isNew ? "Ajouter" : "Enregistrer"}
          </button>
        </footer>
      </form>
    </div>
  );
}
