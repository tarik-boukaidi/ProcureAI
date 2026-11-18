import { useEffect, useState } from "react";
import { TriangleAlert, X } from "lucide-react";
import { createUser, getUsers, updateUser } from "../../users/services/userService";
import { createSupplier, deleteSupplier, updateSupplier } from "../services/supplierService";

// Code HTTP renvoye par supplier-service ou auth-service -> message clair
function errorMessage(status) {
  if (status === 409) return "Ce CIN ou cet email est déjà utilisé.";
  if (status === 400) return "Vérifiez les champs : nom, CIN, email valide et mot de passe de 6 caractères minimum.";
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
    password: "",
  });
  const [account, setAccount] = useState(isNew ? null : undefined);
  const passwordRequired = isNew || account === null;
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (isNew) return;
    getUsers()
      .then((users) => setAccount(users.find((user) => user.role === "SUPPLIER" && Number(user.supplierId) === Number(supplier.id)) ?? null))
      .catch(() => setAccount(null));
  }, [isNew, supplier.id]);

  function update(field, value) {
    setForm({ ...form, [field]: value });
  }

  function accountPayload(supplierId) {
    return {
      name: form.name,
      email: form.email.trim(),
      password: form.password,
      phone: form.phone,
      address: [form.address, form.city].filter(Boolean).join(", "),
      role: "SUPPLIER",
      supplierId,
    };
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setSaving(true);
    const { password, ...fiche } = form;
    try {
      if (isNew) {
        const created = await createSupplier(fiche);
        try {
          await createUser(accountPayload(created.id));
        } catch (err) {
          await deleteSupplier(created.id).catch(() => {});
          throw err;
        }
      } else {
        await updateSupplier(supplier.id, fiche);
        if (account && form.email.trim()) {
          await updateUser(account.id, accountPayload(supplier.id));
        } else if (password) {
          await createUser(accountPayload(supplier.id));
        }
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
              <span>Email {isNew ? "*" : ""}</span>
              <input
                type="email"
                value={form.email}
                onChange={(e) => update("email", e.target.value)}
                placeholder="contact@atlas.ma"
                required={isNew || Boolean(form.password)}
              />
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

          <label className="field">
            <span>{passwordRequired ? "Mot de passe de connexion *" : "Nouveau mot de passe"}</span>
            <input
              type="password"
              value={form.password}
              onChange={(e) => update("password", e.target.value)}
              placeholder={passwordRequired ? "6 caractères minimum" : "Laisser vide pour ne pas le changer"}
              minLength={6}
              required={passwordRequired}
              autoComplete="new-password"
            />
          </label>
          <p className="drawer-hint">Le fournisseur se connecte avec cet email et ce mot de passe.</p>
        </div>

        <footer className="drawer-footer">
          <button type="button" className="btn btn-outline" onClick={onClose}>Annuler</button>
          <button type="submit" className="btn btn-create" disabled={saving || account === undefined}>
            {saving ? "Enregistrement…" : isNew ? "Ajouter" : "Enregistrer"}
          </button>
        </footer>
      </form>
    </div>
  );
}
