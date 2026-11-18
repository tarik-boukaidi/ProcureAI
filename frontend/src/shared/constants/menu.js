import {
  BookOpen,
  Building2,
  FileText,
  GitCompare,
  Inbox,
  LayoutDashboard,
  PackageCheck,
  ShoppingCart,
  Users,
  Wallet,
} from "lucide-react";

// Menu de la sidebar. "roles" : si present, seuls ces roles voient le groupe.
const COMPANY = ["ADMIN", "MANAGER", "EMPLOYEE", "COMPTABLE", "MAGASINIER"];

export const MENU = [
  {
    roles: ["SUPPLIER"],
    items: [{ label: "Mes commandes", path: "/commandes", icon: ShoppingCart }],
  },
  {
    title: null,
    roles: COMPANY,
    items: [{ label: "Tableau de bord", path: "/dashboard", icon: LayoutDashboard }],
  },
  {
    title: "Achats",
    roles: COMPANY,
    items: [
      { label: "Demandes", path: "/demandes", icon: Inbox },
      { label: "Commandes", path: "/commandes", icon: ShoppingCart },
      { label: "Réceptions", path: "/receptions", icon: PackageCheck },
      { label: "Fournisseurs", path: "/fournisseurs", icon: Building2 },
    ],
  },
  {
    title: "Finance",
    roles: ["COMPTABLE", "ADMIN"],
    items: [
      { label: "Factures", path: "/factures", icon: FileText },
      { label: "Rapprochement", path: "/rapprochement", icon: GitCompare },
      { label: "Paiements", path: "/paiements", icon: Wallet },
      { label: "Comptabilité", path: "/comptabilite", icon: BookOpen },
    ],
  },
  {
    title: "Équipe",
    roles: ["ADMIN", "MANAGER"],
    items: [{ label: "Utilisateurs", path: "/utilisateurs", icon: Users }],
  },
];

// Toutes les pages du menu, dans une seule liste
export const ALL_PAGES = MENU.flatMap((group) => group.items);
