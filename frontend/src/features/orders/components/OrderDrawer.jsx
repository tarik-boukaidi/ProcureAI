import { useEffect, useState } from "react";
import { Plus, TriangleAlert, X } from "lucide-react";
import { createOrder } from "../services/orderService";
import { getDemandes } from "../../demandes/services/demandeService";
import { getProducts, getSuppliers } from "../../suppliers/services/supplierService";
import "./OrderDrawer.css";

function emptyLine() {
  return { key: crypto.randomUUID(), productName: "", productId: "", quantity: 1, unitPrice: "" };
}

function linesFromDemande(demande, products) {
  const lines = (demande.items ?? []).map((item) => {
    const match = products.find((product) => product.name.toLowerCase() === item.product.toLowerCase());
    return {
      key: crypto.randomUUID(),
      productName: item.product,
      productId: match ? String(match.id) : "",
      quantity: item.quantity,
      unitPrice: match?.price ?? "",
    };
  });
  return lines.length > 0 ? lines : [emptyLine()];
}

export default function OrderDrawer({ onClose, onSaved }) {
  const [demandes, setDemandes] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [products, setProducts] = useState([]);
  const [demandeId, setDemandeId] = useState("");
  const [supplierId, setSupplierId] = useState("");
  const [deliveryDate, setDeliveryDate] = useState("");
  const [lines, setLines] = useState([emptyLine()]);
  const [loading, setLoading] = useState(true);
  const [loadingProducts, setLoadingProducts] = useState(false);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    Promise.all([getDemandes("ADMIN"), getSuppliers()])
      .then(([allDemandes, allSuppliers]) => {
        setDemandes(allDemandes.filter((demande) => demande.status === "APPROVED"));
        setSuppliers(allSuppliers);
      })
      .catch(() => setError("Impossible de charger les demandes et les fournisseurs."))
      .finally(() => setLoading(false));
  }, []);

  function handleDemande(id) {
    setDemandeId(id);
    const demande = demandes.find((item) => String(item.id) === id);
    setLines(demande ? linesFromDemande(demande, products) : [emptyLine()]);
  }

  async function handleSupplier(id) {
    setSupplierId(id);
    setProducts([]);
    if (!id) {
      setLines((current) => current.map((line) => ({ ...line, productId: "" })));
      return;
    }
    setLoadingProducts(true);
    setError("");
    try {
      const list = await getProducts(id);
      setProducts(list);
      setLines((current) =>
        current.map((line) => {
          const match = list.find((product) => product.name.toLowerCase() === line.productName.toLowerCase());
          if (!match) return { ...line, productId: "" };
          return { ...line, productId: String(match.id), unitPrice: match.price ?? "" };
        })
      );
    } catch {
      setError("Impossible de charger les produits de ce fournisseur.");
    } finally {
      setLoadingProducts(false);
    }
  }

  function updateLine(key, field, value) {
    setLines((current) =>
      current.map((line) => {
        if (line.key !== key) return line;
        if (field !== "productId") return { ...line, [field]: value };
        const product = products.find((item) => String(item.id) === value);
        return {
          ...line,
          productId: value,
          productName: product?.name ?? line.productName,
          unitPrice: product?.price ?? line.unitPrice,
        };
      })
    );
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    if (lines.some((line) => !line.productId || Number(line.unitPrice) <= 0 || Number(line.quantity) < 1)) {
      setError("Chaque article doit avoir un produit du fournisseur, une quantité et un prix supérieurs à 0.");
      return;
    }
    setSaving(true);
    try {
      await createOrder({
        purchaseRequestId: Number(demandeId),
        supplierId: Number(supplierId),
        expectedDeliveryDate: deliveryDate || null,
        lines: lines.map((line) => ({
          productId: Number(line.productId),
          productName: products.find((product) => String(product.id) === line.productId)?.name ?? line.productName,
          quantity: Number(line.quantity),
          unitPrice: Number(line.unitPrice),
        })),
      });
      onSaved();
    } catch (err) {
      setError(
        err.status === 400
          ? "La demande doit être approuvée, et chaque prix doit être supérieur à 0."
          : "Création impossible. Réessayez dans un instant."
      );
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="drawer-overlay" onClick={onClose}>
      <form className="drawer drawer-wide" onClick={(e) => e.stopPropagation()} onSubmit={handleSubmit}>
        <header className="drawer-header">
          <h2>Nouvelle commande</h2>
          <button type="button" className="icon-button" onClick={onClose} title="Fermer">
            <X size={18} />
          </button>
        </header>

        <div className="drawer-body">
          {error && (
            <div className="form-error" role="alert">
              <TriangleAlert size={16} /> {error}
            </div>
          )}

          {loading ? (
            <p className="page-loading">Chargement…</p>
          ) : (
            <>
              <label className="field">
                <span>Demande approuvée *</span>
                <select value={demandeId} onChange={(e) => handleDemande(e.target.value)} required autoFocus>
                  <option value="">Choisir une demande</option>
                  {demandes.map((demande) => (
                    <option key={demande.id} value={demande.id}>
                      #{demande.id} — {demande.items.map((item) => item.product).join(", ")}
                    </option>
                  ))}
                </select>
              </label>
              {demandes.length === 0 && (
                <p className="drawer-hint">Aucune demande approuvée. Approuvez d'abord une demande d'achat.</p>
              )}

              <label className="field">
                <span>Fournisseur *</span>
                <select value={supplierId} onChange={(e) => handleSupplier(e.target.value)} required>
                  <option value="">Choisir un fournisseur</option>
                  {suppliers.map((supplier) => (
                    <option key={supplier.id} value={supplier.id}>{supplier.name}</option>
                  ))}
                </select>
              </label>

              <label className="field">
                <span>Livraison prévue</span>
                <input type="date" value={deliveryDate} onChange={(e) => setDeliveryDate(e.target.value)} />
              </label>

              <h3 className="detail-section">Articles</h3>
              {supplierId && !loadingProducts && products.length === 0 && (
                <p className="drawer-hint">Ce fournisseur n'a aucun produit. Ajoutez-en dans Fournisseurs.</p>
              )}
              {lines.map((line) => (
                <div className="order-line" key={line.key}>
                  <label className="field">
                    <span>Produit *</span>
                    <select
                      value={line.productId}
                      onChange={(e) => updateLine(line.key, "productId", e.target.value)}
                      required
                      disabled={!supplierId || loadingProducts}
                    >
                      <option value="">{line.productName || "Choisir un produit"}</option>
                      {products.map((product) => (
                        <option key={product.id} value={product.id}>{product.name}</option>
                      ))}
                    </select>
                  </label>
                  <label className="field">
                    <span>Qté *</span>
                    <input
                      type="number"
                      min="1"
                      value={line.quantity}
                      onChange={(e) => updateLine(line.key, "quantity", e.target.value)}
                      required
                    />
                  </label>
                  <label className="field">
                    <span>Prix *</span>
                    <input
                      type="number"
                      min="0.01"
                      step="0.01"
                      value={line.unitPrice}
                      onChange={(e) => updateLine(line.key, "unitPrice", e.target.value)}
                      required
                    />
                  </label>
                  <button
                    type="button"
                    className="icon-button order-line-remove"
                    title="Retirer"
                    onClick={() => setLines((current) => current.filter((item) => item.key !== line.key))}
                    disabled={lines.length === 1}
                  >
                    <X size={16} />
                  </button>
                </div>
              ))}
              <button type="button" className="btn btn-outline btn-sm" onClick={() => setLines((current) => [...current, emptyLine()])}>
                <Plus size={14} /> Ajouter un article
              </button>
              <p className="drawer-hint">La commande est créée en brouillon. Vous pourrez l'envoyer au fournisseur ensuite.</p>
            </>
          )}
        </div>

        <footer className="drawer-footer">
          <button type="button" className="btn btn-outline" onClick={onClose}>Annuler</button>
          <button type="submit" className="btn btn-create" disabled={saving || loading || demandes.length === 0}>
            {saving ? "Création…" : "Créer le brouillon"}
          </button>
        </footer>
      </form>
    </div>
  );
}
