import { ArrowRight } from "lucide-react";
import { Link } from "react-router-dom";

export default function FinalCta() {
  return (
    <section className="final-cta" id="demo">
      <div className="container final-cta-inner">
        <h2 className="title">
          Reprenez la main <em>sur vos achats.</em>
        </h2>
        <div className="final-cta-actions">
          <a href="#demo" className="btn btn-accent">
            Demander une démo <ArrowRight size={16} />
          </a>
          <Link to="/login" className="btn btn-light-outline">Se connecter</Link>
        </div>
      </div>
    </section>
  );
}
