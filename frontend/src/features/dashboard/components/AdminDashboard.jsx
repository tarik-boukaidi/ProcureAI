import { useEffect, useState } from "react";
import { FileText, Inbox, TriangleAlert, Wallet } from "lucide-react";
import StatCard from "../../../shared/components/StatCard";
import MonthlyChart from "../../../shared/components/MonthlyChart";
import ConformityGauge from "./ConformityGauge";
import RecentInvoices from "./RecentInvoices";
import { loadAdminDashboard } from "../services/dashboardService";
import { formatMoney } from "../../../shared/utils/format";

export default function AdminDashboard() {
  const year = new Date().getFullYear();
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    loadAdminDashboard(year)
      .then(setData)
      .catch((err) => setError(err.message));
  }, [year]);

  if (error) return <p className="page-error">Impossible de charger le tableau de bord. {error}</p>;
  if (!data) return <p className="page-loading">Chargement…</p>;

  return (
    <>
      <div className="stats">
        <StatCard
          icon={Wallet}
          label="Reste à payer"
          value={`${formatMoney(data.outstanding.remaining)} MAD`}
          hint={`${formatMoney(data.outstanding.paid)} MAD déjà payés`}
        />
        <StatCard icon={FileText} label="Factures à payer" value={data.toPayCount} hint="Validées, en attente de paiement" />
        <StatCard icon={Inbox} label="Demandes en attente" value={data.pendingCount} hint="À approuver par un manager" />
        <StatCard icon={TriangleAlert} label="Écarts détectés" value={data.mismatched} hint="Factures rejetées au rapprochement" />
      </div>

      <div className="dashboard-row">
        <MonthlyChart report={data.report} year={year} />
        <ConformityGauge matched={data.matched} mismatched={data.mismatched} />
      </div>

      <RecentInvoices invoices={data.recentInvoices} />
    </>
  );
}
