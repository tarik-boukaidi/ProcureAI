import { useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { ArrowRight, Eye, EyeOff, LoaderCircle, TriangleAlert } from "lucide-react";
import Logo from "../../../shared/components/Logo";
import LoginShowcase from "../components/LoginShowcase";
import { getCurrentUser, login } from "../services/authService";
import "./LoginPage.css";

export default function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  // Deja connecte : pas besoin de revoir le formulaire
  if (getCurrentUser()) {
    return <Navigate to="/dashboard" replace />;
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login(email, password);
      navigate("/dashboard");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login">
      <div className="login-side">
        <header className="login-header">
          <Logo />
          <span className="login-header-text">
            Pas encore client ? <a href="/#demo">Demander une démo</a>
          </span>
        </header>

        <main className="login-main">
          <form className="login-form" onSubmit={handleSubmit}>
            <h1 className="login-title">
              Bon retour <em>parmi nous.</em>
            </h1>
            <p className="login-subtitle">Connectez-vous pour suivre vos demandes, commandes et factures.</p>

            {error && (
              <div className="login-error" role="alert">
                <TriangleAlert size={16} />
                {error}
              </div>
            )}

            <label className="field">
              <span>Email professionnel</span>
              <input
                type="email"
                placeholder="nom@entreprise.ma"
                autoComplete="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoFocus
              />
            </label>

            <label className="field">
              <span>Mot de passe</span>
              <div className="field-password">
                <input
                  type={showPassword ? "text" : "password"}
                  placeholder="••••••••"
                  autoComplete="current-password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
                <button
                  type="button"
                  className="field-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label={showPassword ? "Masquer le mot de passe" : "Afficher le mot de passe"}
                >
                  {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </label>

            <button type="submit" className="btn btn-dark login-submit" disabled={loading}>
              {loading ? (
                <><LoaderCircle size={16} className="spin" /> Connexion…</>
              ) : (
                <>Se connecter <ArrowRight size={16} /></>
              )}
            </button>

            <p className="login-help">
              Mot de passe oublié ? Contactez l'administrateur de votre entreprise.
            </p>
          </form>
        </main>

        <footer className="login-footer">
          <span>© {new Date().getFullYear()} ProcureAI</span>
          <span>Casablanca, Maroc</span>
        </footer>
      </div>

      <LoginShowcase />
    </div>
  );
}
