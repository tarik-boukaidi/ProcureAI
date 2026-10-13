import { useState } from "react";
import { TriangleAlert, X } from "lucide-react";
import { USER_ROLE } from "../../../shared/constants/status";
import { createUser, updateUser } from "../services/userService";

// Code HTTP renvoye par auth-service -> message clair pour l'utilisateur
function errorMessage(status) {
  if (status === 409) return "Cet email est déjà utilisé par un autre compte.";
  if (status === 400) return "Vérifiez les champs : nom, email valide et mot de passe de 6 caractères minimum.";
  if (status === 403) return "Vous n'avez pas le droit de gérer ce rôle.";
  return "Enregistrement impossible. Réessayez dans un instant.";
}

// Panneau a droite pour ajouter (user sans id) ou modifier un compte.
// roles : les roles que l'utilisateur connecte peut attribuer
export default function UserDrawer({ user, roles, onClose, onSaved }) {
  const isNew = !user.id;
  const [form, setForm] = useState({
    name: user.name ?? "",
    email: user.email ?? "",
    role: user.role ?? roles[roles.length - 1],
    phone: user.phone ?? "",
    address: user.address ?? "",
    password: "",
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
        await createUser(form);
      } else {
        await updateUser(user.id, form);
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
          <h2>{isNew ? "Nouvel utilisateur" : "Modifier l'utilisateur"}</h2>
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
            <span>Nom complet *</span>
            <input value={form.name} onChange={(e) => update("name", e.target.value)} placeholder="Sara Bennani" required autoFocus />
          </label>

          <div className="form-row">
            <label className="field">
              <span>Email *</span>
              <input type="email" value={form.email} onChange={(e) => update("email", e.target.value)} placeholder="sara@company.com" required />
            </label>
            {/* Un manager ne cree que des employes : pas de choix a faire */}
            {roles.length > 1 && (
              <label className="field">
                <span>Rôle</span>
                <select value={form.role} onChange={(e) => update("role", e.target.value)}>
                  {roles.map((role) => (
                    <option key={role} value={role}>{USER_ROLE[role].label}</option>
                  ))}
                </select>
              </label>
            )}
          </div>

          <div className="form-row">
            <label className="field">
              <span>Téléphone</span>
              <input value={form.phone} onChange={(e) => update("phone", e.target.value)} placeholder="0661 00 00 00" />
            </label>
            <label className="field">
              <span>Adresse</span>
              <input value={form.address} onChange={(e) => update("address", e.target.value)} placeholder="Casablanca" />
            </label>
          </div>

          <label className="field">
            <span>{isNew ? "Mot de passe *" : "Nouveau mot de passe"}</span>
            <input
              type="password"
              value={form.password}
              onChange={(e) => update("password", e.target.value)}
              placeholder={isNew ? "6 caractères minimum" : "Laisser vide pour ne pas le changer"}
              minLength={6}
              required={isNew}
              autoComplete="new-password"
            />
          </label>
        </div>

        <footer className="drawer-footer">
          <button type="button" className="btn btn-outline" onClick={onClose}>Annuler</button>
          <button type="submit" className="btn btn-dark" disabled={saving}>
            {saving ? "Enregistrement…" : isNew ? "Créer le compte" : "Enregistrer"}
          </button>
        </footer>
      </form>
    </div>
  );
}
