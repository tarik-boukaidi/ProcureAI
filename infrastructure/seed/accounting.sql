-- Hibernate (ddl-auto=update) ne met pas a jour la contrainte sur type :
-- une base creee avant l'ajout de VAT_RECORDED doit etre corrigee une fois.
ALTER TABLE accounting_entries DROP CONSTRAINT IF EXISTS accounting_entries_type_check;
ALTER TABLE accounting_entries ADD CONSTRAINT accounting_entries_type_check
  CHECK (type IN ('INVOICE_RECORDED', 'VAT_RECORDED', 'PAYMENT_RECORDED'));

TRUNCATE accounting_entries RESTART IDENTITY CASCADE;

-- Facture validee : 6111 Achats (HT) + 3455 TVA recuperable / 4411 Fournisseurs (TTC).
-- Paiement : 4411 Fournisseurs / 5141 Banques (TTC).
INSERT INTO accounting_entries (type, invoice_id, supplier_id, amount, debit_account, credit_account, description, entry_date) VALUES
  ('INVOICE_RECORDED', 4, 1, 3900.00, '6111', '4411', 'Facture FA-ATL-2026-097 a payer', current_date - 22),
  ('VAT_RECORDED', 4, 1, 780.00, '3455', '4411', 'TVA facture FA-ATL-2026-097', current_date - 22),
  ('INVOICE_RECORDED', 1, 1, 7470.00, '6111', '4411', 'Facture FA-ATL-2026-118 a payer', current_date - 15),
  ('VAT_RECORDED', 1, 1, 1494.00, '3455', '4411', 'TVA facture FA-ATL-2026-118', current_date - 15),
  ('PAYMENT_RECORDED', 1, 1, 8964.00, '4411', '5141', 'Paiement facture FA-ATL-2026-118', current_date - 6);
