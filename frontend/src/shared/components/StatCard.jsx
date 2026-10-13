import "./StatCard.css";

export default function StatCard({ icon: Icon, label, value, hint }) {
  return (
    <div className="card stat-card">
      <div className="stat-card-head">
        <span>{label}</span>
        <Icon size={16} strokeWidth={1.75} />
      </div>
      <strong className="stat-card-value mono">{value}</strong>
      {hint && <span className="stat-card-hint">{hint}</span>}
    </div>
  );
}
