// Onglets de filtre avec le nombre d'elements dans chaque onglet.
// filters : [{ value, label, test }] — "test(item)" dit si l'element est dans l'onglet (absent = tous)
export default function StatusFilter({ filters, items, value, onChange }) {
  return (
    <div className="segmented">
      {filters.map((f) => (
        <button key={f.value} className={value === f.value ? "active" : ""} onClick={() => onChange(f.value)}>
          {f.label} <span className="mono">{f.test ? items.filter(f.test).length : items.length}</span>
        </button>
      ))}
    </div>
  );
}

// Garde les elements de l'onglet choisi
export function applyFilter(filters, value, items) {
  const filter = filters.find((f) => f.value === value);
  return filter?.test ? items.filter(filter.test) : items;
}
