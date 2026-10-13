import { Outlet } from "react-router-dom";
import Sidebar from "./Sidebar";
import Topbar from "./Topbar";
import "./AppLayout.css";

// Cadre commun a toutes les pages de l'application (apres connexion)
export default function AppLayout() {
  return (
    <div className="app-layout">
      <Sidebar />
      <div className="app-content">
        <Topbar />
        <main className="app-page">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
