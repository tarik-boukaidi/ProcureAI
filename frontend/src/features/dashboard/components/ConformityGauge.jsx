const RADIUS = 52;
const CIRCLE = 2 * Math.PI * RADIUS;

// Part des factures rapprochees sans ecart (MATCHED) parmi toutes les factures controlees
export default function ConformityGauge({ matched, mismatched }) {
  const total = matched + mismatched;
  const rate = total === 0 ? 0 : matched / total;

  return (
    <div className="card gauge-card">
      <div className="card-head">
        <div>
          <h2>Taux de conformité</h2>
          <span className="card-subtitle">Rapprochement 3 voies</span>
        </div>
      </div>

      <div className="gauge">
        <svg viewBox="0 0 120 120">
          <circle className="gauge-track" cx="60" cy="60" r={RADIUS} />
          <circle
            className="gauge-value"
            cx="60"
            cy="60"
            r={RADIUS}
            strokeDasharray={CIRCLE}
            strokeDashoffset={CIRCLE * (1 - rate)}
          />
        </svg>
        <strong className="gauge-text mono">{total === 0 ? "—" : `${Math.round(rate * 100)} %`}</strong>
      </div>

      <ul className="gauge-legend">
        <li><span className="dot dot-green" /> Conformes <b className="mono">{matched}</b></li>
        <li><span className="dot dot-red" /> Avec écart <b className="mono">{mismatched}</b></li>
      </ul>
    </div>
  );
}
