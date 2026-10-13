import Logo from "../../../shared/components/Logo";

const COLUMNS = [
  { title: "Produit", links: ["Demandes", "Commandes", "Factures", "Paiements", "Comptabilité"] },
  { title: "Ressources", links: ["Documentation", "API", "Sécurité"] },
  { title: "Entreprise", links: ["À propos", "Contact"] },
];

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container">
        <div className="footer-top">
          <div className="footer-brand">
            <Logo light />
            <p>Le logiciel achats et comptes fournisseurs pensé pour les entreprises marocaines.</p>
          </div>
          {COLUMNS.map((col) => (
            <div key={col.title} className="footer-col">
              <span>{col.title}</span>
              {col.links.map((link) => <a key={link} href="#top">{link}</a>)}
            </div>
          ))}
        </div>
        <div className="footer-bottom">
          <span>© {new Date().getFullYear()} ProcureAI</span>
          <span>Casablanca, Maroc</span>
        </div>
      </div>
    </footer>
  );
}
