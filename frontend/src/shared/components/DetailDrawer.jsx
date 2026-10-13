import { X } from "lucide-react";

// Panneau a droite (detail d'une commande, d'une facture...). footer : boutons d'action optionnels.
export default function DetailDrawer({ title, subtitle, onClose, footer, children }) {
  return (
    <div className="drawer-overlay" onClick={onClose}>
      <aside className="drawer" onClick={(e) => e.stopPropagation()}>
        <header className="drawer-header">
          <div>
            <h2>{title}</h2>
            {subtitle && <span className="card-subtitle">{subtitle}</span>}
          </div>
          <button type="button" className="icon-button" onClick={onClose} title="Fermer">
            <X size={18} />
          </button>
        </header>
        <div className="drawer-body">{children}</div>
        {footer && <footer className="drawer-footer">{footer}</footer>}
      </aside>
    </div>
  );
}
