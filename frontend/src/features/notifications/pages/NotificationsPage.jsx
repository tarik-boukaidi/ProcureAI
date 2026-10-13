import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Bell, CheckCheck } from "lucide-react";
import StatusFilter, { applyFilter } from "../../../shared/components/StatusFilter";
import NotificationItem, { notificationLink } from "../components/NotificationItem";
import { getMyNotifications, markAsRead } from "../services/notificationService";
import "./NotificationsPage.css";

const FILTERS = [
  { value: "ALL", label: "Toutes" },
  { value: "UNREAD", label: "Non lues", test: (n) => !n.read },
];

export default function NotificationsPage() {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState(null);
  const [error, setError] = useState("");
  const [filter, setFilter] = useState("ALL");

  function refresh() {
    getMyNotifications()
      .then(setNotifications)
      .catch((err) => setError(err.message));
  }

  useEffect(() => {
    refresh();
  }, []);

  async function handleOpen(notification) {
    if (!notification.read) await markAsRead([notification.id]).catch(() => {});
    navigate(notificationLink(notification));
  }

  async function handleReadAll() {
    const unreadIds = notifications.filter((n) => !n.read).map((n) => n.id);
    await markAsRead(unreadIds).catch(() => {});
    refresh();
  }

  if (error) return <p className="page-error">Impossible de charger les notifications. {error}</p>;
  if (!notifications) return <p className="page-loading">Chargement…</p>;

  const unreadCount = notifications.filter((n) => !n.read).length;
  const visible = applyFilter(FILTERS, filter, notifications).sort((a, b) => b.createdAt.localeCompare(a.createdAt));

  return (
    <div className="notifications-page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Notifications</h1>
          <p className="page-subtitle">
            {unreadCount > 0 ? `${unreadCount} notification(s) non lue(s).` : "Vous êtes à jour."}
          </p>
        </div>
        {unreadCount > 0 && (
          <button className="btn btn-outline" onClick={handleReadAll}>
            <CheckCheck size={16} /> Tout marquer comme lu
          </button>
        )}
      </div>

      <div className="card">
        <div className="toolbar">
          <StatusFilter filters={FILTERS} items={notifications} value={filter} onChange={setFilter} />
        </div>

        {notifications.length === 0 ? (
          <div className="empty-state">
            <Bell size={28} strokeWidth={1.5} />
            <h3>Aucune notification</h3>
            <p>Vous serez prévenu ici quand une demande ou une facture vous concerne.</p>
          </div>
        ) : visible.length === 0 ? (
          <p className="empty">Aucune notification non lue.</p>
        ) : (
          <ul className="notification-list">
            {visible.map((notification) => (
              <li key={notification.id}>
                <NotificationItem notification={notification} onOpen={handleOpen} />
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
