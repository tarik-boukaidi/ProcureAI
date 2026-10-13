import { useState } from "react";
import { TriangleAlert, X } from "lucide-react";
import { createDemande } from "../services/demandeService";

// Panneau a droite pour creer une demande d'achat
export default function DemandeDrawer({ onClose, onSaved }) {
  const [form, setForm] = useState({ product: "", quantity: 1, reason: "" });
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
      await createDemande({ ...form, quantity: Number(form.quantity) });
      onSaved();
    } catch (err) {
      setError(
        err.status === 400
          ? "Indiquez un produit et une quantité supérieure à 0."
          : "Envoi impossible. Réessayez dans un instant."
      );
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="drawer-overlay" onClick={onClose}>
      {/* stopPropagation : un clic dans le panneau ne doit pas le fermer */}
      <form className="drawer" onClick={(e) => e.stopPropagation()} onSubmit={handleSubmit}>
        <header className="drawer-header">
          <h2>Nouvelle demande</h2>
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

          <div className="form-row demande-form-row">
            <label className="field">
              <span>Produit *</span>
              <input value={form.product} onChange={(e) => update("product", e.target.value)} placeholder="Écran Dell 27 pouces" required autoFocus />
            </label>
            <label className="field">
              <span>Quantité *</span>
              <input type="number" min="1" value={form.quantity} onChange={(e) => update("quantity", e.target.value)} required />
            </label>
          </div>

          <label className="field">
            <span>Motif</span>
            <textarea
              rows="4"
              value={form.reason}
              onChange={(e) => update("reason", e.target.value)}
              placeholder="Pourquoi avez-vous besoin de cet achat ?"
            />
          </label>

          <p className="drawer-hint">Votre manager reçoit une notification et valide ou rejette la demande.</p>
        </div>

        <footer className="drawer-footer">
          <button type="button" className="btn btn-outline" onClick={onClose}>Annuler</button>
          <button type="submit" className="btn btn-dark" disabled={saving}>
            {saving ? "Envoi…" : "Envoyer la demande"}
          </button>
        </footer>
      </form>
    </div>
  );
}
