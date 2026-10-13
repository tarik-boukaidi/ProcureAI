// Statut renvoye par le backend -> texte affiche + couleur du tag (voir .tag-* dans global.css)

export const ORDER_STATUS = {
  DRAFT: { label: "Brouillon", tone: "ink" },
  SENT: { label: "Envoyée", tone: "amber" },
  CONFIRMED: { label: "Confirmée", tone: "amber" },
  DELIVERED: { label: "Livrée", tone: "green" },
  CANCELLED: { label: "Annulée", tone: "red" },
};

export const INVOICE_STATUS = {
  RECEIVED: { label: "À rapprocher", tone: "amber" },
  VALIDATED: { label: "Validée", tone: "green" },
  REJECTED: { label: "Rejetée", tone: "red" },
  PAID: { label: "Payée", tone: "ink" },
};

export const MATCH_STATUS = {
  MATCHED: { label: "Conforme", tone: "green" },
  MISMATCH: { label: "Écarts", tone: "red" },
};

export const PAYMENT_METHOD = {
  BANK_TRANSFER: "Virement",
  CHECK: "Chèque",
  CARD: "Carte",
};

export const USER_ROLE = {
  ADMIN: { label: "Admin", tone: "ink" },
  MANAGER: { label: "Manager", tone: "amber" },
  EMPLOYEE: { label: "Employé", tone: "green" },
  MAGASINIER: { label: "Magasinier", tone: "ink" },
  COMPTABLE: { label: "Comptable", tone: "blue" },
};

// Comptes du plan comptable marocain utilises par accounting-service
export const ACCOUNTS = {
  6111: "Achats",
  3455: "TVA récupérable",
  4411: "Fournisseurs",
  5141: "Banques",
};

// Statut inconnu : on affiche la valeur brute en gris
export function statusOf(map, value) {
  return map[value] ?? { label: value ?? "—", tone: "ink" };
}
