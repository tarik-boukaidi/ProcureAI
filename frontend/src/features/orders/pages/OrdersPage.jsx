import { useEffect, useState } from "react";
import { Search, ShoppingCart } from "lucide-react";
import OrderDetail from "../components/OrderDetail";
import StatusFilter, { applyFilter } from "../../../shared/components/StatusFilter";
import { loadOrders } from "../services/orderService";
import { getCurrentUser } from "../../auth/services/authService";
import { ORDER_STATUS, statusOf } from "../../../shared/constants/status";
import { formatDate, formatMoney } from "../../../shared/utils/format";

const FILTERS = [
  { value: "ALL", label: "Toutes" },
  { value: "DRAFT", label: "Brouillons", test: (o) => o.status === "DRAFT" },
  { value: "IN_PROGRESS", label: "En cours", test: (o) => o.status === "SENT" || o.status === "CONFIRMED" },
  { value: "DELIVERED", label: "Livrées", test: (o) => o.status === "DELIVERED" },
  { value: "CANCELLED", label: "Annulées", test: (o) => o.status === "CANCELLED" },
];

export default function OrdersPage() {
  const canEdit = getCurrentUser().role === "ADMIN";
  const [orders, setOrders] = useState(null);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState("ALL");
  const [selected, setSelected] = useState(null);

  useEffect(() => {
    loadOrders()
      .then(setOrders)
      .catch((err) => setError(err.message));
  }, []);

  // order-service renvoie la commande sans le nom du fournisseur : on garde celui deja charge
  function handleChanged(updated) {
    const order = { ...selected, ...updated };
    setOrders(orders.map((o) => (o.id === order.id ? order : o)));
    setSelected(order);
  }

  if (error) return <p className="page-error">Impossible de charger les commandes. {error}</p>;
  if (!orders) return <p className="page-loading">Chargement…</p>;

  const visible = applyFilter(FILTERS, filter, orders)
    .filter((o) => `${o.orderNumber} ${o.supplierName}`.toLowerCase().includes(search.toLowerCase()))
    .sort((a, b) => b.id - a.id);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Commandes</h1>
          <p className="page-subtitle">Les bons de commande envoyés aux fournisseurs.</p>
        </div>
      </div>

      <div className="card table-card">
        <div className="toolbar">
          <StatusFilter filters={FILTERS} items={orders} value={filter} onChange={setFilter} />
          <label className="search">
            <Search size={16} />
            <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Rechercher un n° ou un fournisseur…" />
          </label>
        </div>

        {orders.length === 0 ? (
          <div className="empty-state">
            <ShoppingCart size={28} strokeWidth={1.5} />
            <h3>Aucune commande</h3>
            <p>Une commande est créée à partir d'une demande d'achat approuvée.</p>
          </div>
        ) : visible.length === 0 ? (
          <p className="empty">Aucune commande ne correspond à votre recherche.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>N° commande</th>
                <th>Fournisseur</th>
                <th>Demande</th>
                <th>Date</th>
                <th>Livraison prévue</th>
                <th className="right">Montant (MAD)</th>
                <th>Statut</th>
              </tr>
            </thead>
            <tbody>
              {visible.map((order) => {
                const status = statusOf(ORDER_STATUS, order.status);
                return (
                  <tr key={order.id} className="clickable" onClick={() => setSelected(order)}>
                    <td className="mono">{order.orderNumber}</td>
                    <td>{order.supplierName}</td>
                    <td className="mono muted">#{order.purchaseRequestId}</td>
                    <td className="muted">{formatDate(order.orderDate)}</td>
                    <td className="muted">{formatDate(order.expectedDeliveryDate)}</td>
                    <td className="mono right">{formatMoney(order.totalAmount)}</td>
                    <td><span className={`tag tag-${status.tone}`}>{status.label}</span></td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>

      {selected && (
        <OrderDetail order={selected} canEdit={canEdit} onClose={() => setSelected(null)} onChanged={handleChanged} />
      )}
    </div>
  );
}
