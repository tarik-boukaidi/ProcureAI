import { formatMoney } from "../utils/format";
import "./MonthlyChart.css";

const MONTHS = ["Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sep", "Oct", "Nov", "Déc"];

// Graphique en barres fait en CSS : la hauteur de chaque barre = total du mois / plus grand total
export default function MonthlyChart({ report, year }) {
  const max = Math.max(...report.map((m) => Number(m.total)), 1);
  const yearTotal = report.reduce((sum, m) => sum + Number(m.total), 0);
  const currentMonth = new Date().getMonth() + 1;

  return (
    <div className="card chart-card">
      <div className="card-head">
        <div>
          <h2>Achats par mois</h2>
          <span className="card-subtitle">Factures enregistrées en {year}</span>
        </div>
        <div className="chart-total">
          <span>Total {year}</span>
          <strong className="mono">{formatMoney(yearTotal)} MAD</strong>
        </div>
      </div>

      <div className="chart">
        {report.map((m) => (
          <div key={m.month} className="chart-column" title={`${MONTHS[m.month - 1]} : ${formatMoney(m.total)} MAD`}>
            <div className="chart-bar-area">
              <div
                className={m.month === currentMonth ? "chart-bar current" : "chart-bar"}
                style={{ height: `${(Number(m.total) / max) * 100}%` }}
              />
            </div>
            <span className="chart-label">{MONTHS[m.month - 1]}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
