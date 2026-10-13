import { apiDelete, apiGet, apiPost, apiPut } from "../../../api/client";

export function getSuppliers() {
  return apiGet("/api/suppliers");
}

// { 3: "Atlas Informatique", ... } pour afficher le nom a partir de supplierId
export async function getSupplierNames() {
  const suppliers = await getSuppliers();
  return Object.fromEntries(suppliers.map((s) => [s.id, s.name]));
}

export function createSupplier(supplier) {
  return apiPost("/api/suppliers", supplier);
}

export function updateSupplier(id, supplier) {
  return apiPut(`/api/suppliers/${id}`, supplier);
}

export function deleteSupplier(id) {
  return apiDelete(`/api/suppliers/${id}`);
}
