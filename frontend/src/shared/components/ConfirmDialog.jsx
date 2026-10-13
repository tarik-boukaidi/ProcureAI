import { useState } from "react";
import { TriangleAlert } from "lucide-react";
import "./ConfirmDialog.css";

// Popup de confirmation pour les actions dangereuses (suppression...).
// onConfirm doit renvoyer une promesse : le popup reste ouvert tant qu'elle n'est pas terminee.
export default function ConfirmDialog({ title, message, confirmLabel = "Supprimer", onConfirm, onClose }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleConfirm() {
    setLoading(true);
    setError("");
    try {
      await onConfirm();
      onClose();
    } catch {
      setError("L'action a échoué. Réessayez dans un instant.");
      setLoading(false);
    }
  }

  return (
    <div className="dialog-overlay" onClick={loading ? undefined : onClose}>
      <div className="dialog" role="alertdialog" aria-modal="true" onClick={(e) => e.stopPropagation()}>
        <div className="dialog-icon">
          <TriangleAlert size={20} />
        </div>
        <h2 className="dialog-title">{title}</h2>
        <p className="dialog-message">{message}</p>

        {error && <p className="dialog-error">{error}</p>}

        <div className="dialog-actions">
          <button className="btn btn-outline" onClick={onClose} disabled={loading}>
            Annuler
          </button>
          <button className="btn btn-danger" onClick={handleConfirm} disabled={loading} autoFocus>
            {loading ? "Patientez…" : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
