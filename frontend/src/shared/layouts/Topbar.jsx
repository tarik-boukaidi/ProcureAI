import { useLocation } from "react-router-dom";
import { ALL_PAGES } from "../constants/menu";
import NotificationBell from "../../features/notifications/components/NotificationBell";

export default function Topbar() {
  const location = useLocation();
  const page = ALL_PAGES.find((p) => p.path === location.pathname);
  const title = page ? page.label : "Notifications";

  return (
    <header className="topbar">
      <span className="topbar-title">{title}</span>
      <NotificationBell />
    </header>
  );
}
