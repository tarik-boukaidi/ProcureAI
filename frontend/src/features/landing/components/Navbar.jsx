import { Link } from "react-router-dom";
import Logo from "../../../shared/components/Logo";

const LINKS = [
  { label: "Produit", href: "#produit" },
  { label: "Fonctionnement", href: "#fonctionnement" },
  { label: "Équipes", href: "#equipes" },
  { label: "FAQ", href: "#faq" },
];

export default function Navbar() {
  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <Logo />
        <nav className="navbar-links">
          {LINKS.map((link) => (
            <a key={link.href} href={link.href}>{link.label}</a>
          ))}
        </nav>
        <div className="navbar-actions">
          <Link to="/login" className="navbar-login">Connexion</Link>
          <a href="#demo" className="btn btn-dark">Demander une démo</a>
        </div>
      </div>
    </header>
  );
}
