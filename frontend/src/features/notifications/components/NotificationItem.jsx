import { Bell, CircleCheck, CircleX, Inbox, Truck, TriangleAlert, Wallet } from "lucide-react";
import { formatDateTime } from "../../../shared/utils/format";
import "./NotificationItem.css";

// Type de notification -> icone, couleur et page a ouvrir au clic
const TYPES = {
  DEMANDE_CREATED: { icon: Inbox, tone: "amber", link: "/demandes" },
  DEMANDE_APPROVED: { icon: CircleCheck, tone: "green", link: "/demandes" },
  DEMANDE_REJECTED: { icon: CircleX, tone: "red", link: "/demandes" },
  INVOICE_REJECTED: { icon: TriangleAlert, tone: "red", link: "/rapprochement" },
  INVOICE_PAID: { icon: Wallet, tone: "green", link: "/paiements" },
  ORDER_SENT: { icon: Truck, tone: "amber", link: "/receptions" },
};

export function notificationLink(notification) {
  return TYPES[notification.type]?.link ?? "/dashboard";
}

// Une notification cliquable (page Notifications et menu de la cloche)
export default function NotificationItem({ notification, onOpen }) {
  const type = TYPES[notification.type] ?? { icon: Bell, tone: "ink" };
  const Icon = type.icon;

  return (
    <button className={notification.read ? "notification" : "notification unread"} onClick={() => onOpen(notification)}>
      <span className={`notification-icon tag-${type.tone}`}>
        <Icon size={16} />
      </span>
      <span className="notification-text">
        <span className="notification-message">{notification.message}</span>
        <span className="notification-date">{formatDateTime(notification.createdAt)}</span>
      </span>
    </button>
  );
}
