import { Link } from "react-router-dom";
import "./Logo.css";

export default function Logo({ light = false }) {
  return (
    <Link to="/" className={light ? "logo logo-light" : "logo"}>
      <span className="logo-mark" />
      ProcureAI
    </Link>
  );
}
