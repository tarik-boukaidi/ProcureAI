import AdminDashboard from "../components/AdminDashboard";
import DemandesDashboard from "../components/DemandesDashboard";
import { getCurrentUser } from "../../auth/services/authService";
import { nameFromEmail } from "../../../shared/utils/format";
import "./DashboardPage.css";

export default function DashboardPage() {
  const user = getCurrentUser();
  const today = new Date().toLocaleDateString("fr-FR", { weekday: "long", day: "numeric", month: "long" });

  return (
    <div className="dashboard">
      <div className="dashboard-greeting">
        <span className="eyebrow">{today}</span>
        <h1>Bonjour, <em>{nameFromEmail(user.email)}.</em></h1>
      </div>

      {user.role === "ADMIN" || user.role === "COMPTABLE" ? <AdminDashboard /> : <DemandesDashboard role={user.role} />}
    </div>
  );
}
