import { NavLink, useNavigate } from "react-router-dom";
import { LogOut } from "lucide-react";
import Logo from "../components/Logo";
import { MENU } from "../constants/menu";
import { nameFromEmail } from "../utils/format";
import { getCurrentUser, logout } from "../../features/auth/services/authService";

export default function Sidebar() {
  const navigate = useNavigate();
  const user = getCurrentUser();

  // On garde seulement les groupes autorises pour ce role
  const groups = MENU.filter((group) => !group.roles || group.roles.includes(user.role));

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <Logo />
      </div>

      <nav className="sidebar-nav">
        {groups.map((group, index) => (
          <div key={index} className="sidebar-group">
            {group.title && <span className="sidebar-group-title">{group.title}</span>}
            {group.items.map(({ label, path, icon: Icon }) => (
              <NavLink key={path} to={path} className="sidebar-link">
                <Icon size={17} strokeWidth={1.75} />
                {label}
              </NavLink>
            ))}
          </div>
        ))}
      </nav>

      <div className="sidebar-user">
        <span className="avatar-initials">{user.email.charAt(0).toUpperCase()}</span>
        <div className="sidebar-user-info">
          <strong>{nameFromEmail(user.email)}</strong>
          <span>{user.role}</span>
        </div>
        <button className="icon-button" onClick={handleLogout} title="Se déconnecter">
          <LogOut size={16} />
        </button>
      </div>
    </aside>
  );
}
