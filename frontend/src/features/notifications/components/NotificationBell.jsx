import { useEffect, useRef, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Bell, CheckCheck } from "lucide-react";
import NotificationItem, { notificationLink } from "./NotificationItem";
import { getMyNotifications, markAsRead, NOTIFICATIONS_CHANGED } from "../services/notificationService";
import "./NotificationBell.css";

const PREVIEW_SIZE = 6;
const REFRESH_EVERY_MS = 30000;

// Cloche de la topbar : badge des non lues + menu avec les dernieres notifications
export default function NotificationBell() {
  const location = useLocation();
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState([]);
  const [open, setOpen] = useState(false);
  const bellRef = useRef(null);

  // Rechargees a chaque page, toutes les 30 s et quand on en marque comme lues
  // (on ignore les erreurs : la cloche reste vide)
  useEffect(() => {
    function refresh() {
      getMyNotifications()
        .then(setNotifications)
        .catch(() => setNotifications([]));
    }

    refresh();
    const timer = setInterval(refresh, REFRESH_EVERY_MS);
    window.addEventListener(NOTIFICATIONS_CHANGED, refresh);
    return () => {
      clearInterval(timer);
      window.removeEventListener(NOTIFICATIONS_CHANGED, refresh);
    };
  }, [location.pathname]);

  // Le menu se ferme au clic en dehors ou avec Echap
  useEffect(() => {
    if (!open) return;
    function handleClick(event) {
      if (!bellRef.current.contains(event.target)) setOpen(false);
    }
    function handleKey(event) {
      if (event.key === "Escape") setOpen(false);
    }
    document.addEventListener("mousedown", handleClick);
    document.addEventListener("keydown", handleKey);
    return () => {
      document.removeEventListener("mousedown", handleClick);
      document.removeEventListener("keydown", handleKey);
    };
  }, [open]);

  const unread = notifications.filter((n) => !n.read);
  const latest = [...notifications].sort((a, b) => b.createdAt.localeCompare(a.createdAt)).slice(0, PREVIEW_SIZE);

  async function handleOpen(notification) {
    setOpen(false);
    if (!notification.read) await markAsRead([notification.id]).catch(() => {});
    navigate(notificationLink(notification));
  }

  function handleReadAll() {
    markAsRead(unread.map((n) => n.id)).catch(() => {});
  }

  return (
    <div className="bell" ref={bellRef}>
      <button className="icon-button topbar-bell" onClick={() => setOpen(!open)} title="Notifications" aria-expanded={open}>
        <Bell size={18} />
        {unread.length > 0 && <span className="topbar-badge">{unread.length > 9 ? "9+" : unread.length}</span>}
      </button>

      {open && (
        <div className="bell-panel">
          <header className="bell-header">
            <strong>Notifications</strong>
            {unread.length > 0 && (
              <button className="bell-read-all" onClick={handleReadAll}>
                <CheckCheck size={14} /> Tout marquer comme lu
              </button>
            )}
          </header>

          {latest.length === 0 ? (
            <p className="bell-empty">Aucune notification pour le moment.</p>
          ) : (
            <ul className="notification-list bell-list">
              {latest.map((notification) => (
                <li key={notification.id}>
                  <NotificationItem notification={notification} onOpen={handleOpen} />
                </li>
              ))}
            </ul>
          )}

          <Link to="/notifications" className="bell-footer" onClick={() => setOpen(false)}>
            Voir tout
          </Link>
        </div>
      )}
    </div>
  );
}
