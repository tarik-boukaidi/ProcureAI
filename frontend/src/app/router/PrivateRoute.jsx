import { Navigate } from "react-router-dom";
import { getCurrentUser } from "../../features/auth/services/authService";

// Page reservee aux utilisateurs connectes, sinon retour au login.
// roles (optionnel) : seuls ces roles entrent, les autres retournent au tableau de bord.
export default function PrivateRoute({ roles, children }) {
  const user = getCurrentUser();
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  if (roles && !roles.includes(user.role)) {
    return <Navigate to="/dashboard" replace />;
  }
  return children;
}
