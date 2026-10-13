import { ArrowRight } from "lucide-react";
import ProductPreview from "./ProductPreview";

export default function Hero() {
  return (
    <section className="hero" id="top">
      <div className="container">
        <span className="eyebrow">Achats & comptes fournisseurs</span>
        <h1 className="hero-title">
          Chaque dirham dépensé, <em>justifié.</em>
        </h1>
        <div className="hero-bottom">
          <p className="lead">
            ProcureAI relie demandes, bons de commande, réceptions et factures. Les écarts sont
            détectés avant le paiement, et les écritures comptables se passent toutes seules.
          </p>
          <div className="hero-actions">
            <a href="#demo" className="btn btn-accent">
              Demander une démo <ArrowRight size={16} />
            </a>
            <a href="#fonctionnement" className="btn btn-outline">Voir le fonctionnement</a>
          </div>
        </div>
      </div>

      <div className="container" id="produit">
        <div className="stage">
          <ProductPreview />
        </div>
      </div>
    </section>
  );
}
