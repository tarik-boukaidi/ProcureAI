-- Colonnes ajoutees apres la creation de la table : on les cree si invoice-service n'a pas encore ete reconstruit
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS vat_rate numeric(5,2);
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS vat_amount numeric(12,2);
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS total_ttc numeric(12,2);

TRUNCATE invoice_lines, invoices RESTART IDENTITY CASCADE;

-- Factures (total_amount = HT, TVA 20 %) :
--   1 PAID      : rapprochee puis payee
--   2 REJECTED  : 4 chaises facturees mais 1 refusee a la reception (ecart de quantite)
--   3 RECEIVED  : 15 ramettes, a rapprocher depuis la page Factures (le rapprochement passera)
--   4 VALIDATED : a payer, echeance depassee
INSERT INTO invoices (invoice_number, purchase_order_id, supplier_id, invoice_date, due_date, total_amount, vat_rate, vat_amount, total_ttc, status) VALUES
  ('FA-ATL-2026-118', 1, 1, current_date - 16, current_date + 14, 7470.00, 20, 1494.00, 8964.00, 'PAID'),
  ('BP-2026-0412', 2, 2, current_date - 12, current_date + 18, 5800.00, 20, 1160.00, 6960.00, 'REJECTED'),
  ('MF-88213', 3, 3, current_date - 1, current_date + 29, 675.00, 20, 135.00, 810.00, 'RECEIVED'),
  ('FA-ATL-2026-097', 4, 1, current_date - 23, current_date - 3, 3900.00, 20, 780.00, 4680.00, 'VALIDATED');

INSERT INTO invoice_lines (product_id, product_name, quantity, unit_price, invoice_id) VALUES
  (1, 'Écran Dell 27 pouces', 3, 2490.00, 1),
  (4, 'Chaise ergonomique Ergo Pro', 4, 1450.00, 2),
  (6, 'Ramette papier A4 (500 f.)', 15, 45.00, 3),
  (3, 'Clavier Logitech MX Keys', 10, 390.00, 4);
