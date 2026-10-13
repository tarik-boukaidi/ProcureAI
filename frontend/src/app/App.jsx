import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import LandingPage from "../features/landing/pages/LandingPage";
import LoginPage from "../features/auth/pages/LoginPage";
import DashboardPage from "../features/dashboard/pages/DashboardPage";
import DemandesPage from "../features/demandes/pages/DemandesPage";
import OrdersPage from "../features/orders/pages/OrdersPage";
import ReceptionsPage from "../features/receptions/pages/ReceptionsPage";
import SuppliersPage from "../features/suppliers/pages/SuppliersPage";
import InvoicesPage from "../features/invoices/pages/InvoicesPage";
import MatchingPage from "../features/matching/pages/MatchingPage";
import PaymentsPage from "../features/payments/pages/PaymentsPage";
import AccountingPage from "../features/accounting/pages/AccountingPage";
import NotificationsPage from "../features/notifications/pages/NotificationsPage";
import UsersPage from "../features/users/pages/UsersPage";
import AppLayout from "../shared/layouts/AppLayout";
import PrivateRoute from "./router/PrivateRoute";

// Pages Finance : le comptable travaille, l'admin consulte (comme dans le menu)
const financeOnly = (page) => <PrivateRoute roles={["COMPTABLE", "ADMIN"]}>{page}</PrivateRoute>;

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<LoginPage />} />

        {/* Pages apres connexion : sidebar + topbar autour */}
        <Route element={<PrivateRoute><AppLayout /></PrivateRoute>}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/demandes" element={<DemandesPage />} />
          <Route path="/commandes" element={<OrdersPage />} />
          <Route path="/receptions" element={<ReceptionsPage />} />
          <Route path="/fournisseurs" element={<SuppliersPage />} />
          <Route path="/notifications" element={<NotificationsPage />} />

          <Route path="/factures" element={financeOnly(<InvoicesPage />)} />
          <Route path="/rapprochement" element={financeOnly(<MatchingPage />)} />
          <Route path="/paiements" element={financeOnly(<PaymentsPage />)} />
          <Route path="/comptabilite" element={financeOnly(<AccountingPage />)} />

          <Route path="/utilisateurs" element={<PrivateRoute roles={["ADMIN", "MANAGER"]}><UsersPage /></PrivateRoute>} />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
