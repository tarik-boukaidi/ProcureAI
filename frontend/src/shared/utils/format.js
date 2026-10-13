const money = new Intl.NumberFormat("fr-FR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });

// 17968.2 -> "17 968,20"
export function formatMoney(value) {
  return money.format(Number(value ?? 0));
}

// "2026-10-31" -> "31 oct."
export function formatDate(value) {
  if (!value) return "—";
  return new Date(value).toLocaleDateString("fr-FR", { day: "numeric", month: "short" });
}

// "2026-10-31" -> "31 oct. 2026"
export function formatFullDate(value) {
  if (!value) return "—";
  return new Date(value).toLocaleDateString("fr-FR", { day: "numeric", month: "short", year: "numeric" });
}

// "2026-10-10T18:32:00" -> "10 oct., 18:32"
export function formatDateTime(value) {
  if (!value) return "—";
  return new Date(value).toLocaleString("fr-FR", { day: "numeric", month: "short", hour: "2-digit", minute: "2-digit" });
}

// Echeance depassee (date avant aujourd'hui)
export function isOverdue(date) {
  return Boolean(date) && date < new Date().toISOString().slice(0, 10);
}

// "tarik@company.com" -> "Tarik"
export function nameFromEmail(email) {
  const name = email.split("@")[0];
  return name.charAt(0).toUpperCase() + name.slice(1);
}
