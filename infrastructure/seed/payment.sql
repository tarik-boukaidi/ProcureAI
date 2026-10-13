TRUNCATE payments RESTART IDENTITY CASCADE;

-- On paie le TTC de la facture 1 (7 470 HT + 1 494 TVA)
INSERT INTO payments (reference, invoice_id, supplier_id, amount, method, payment_date) VALUES
  ('PAY-7C21E9A4', 1, 1, 8964.00, 'BANK_TRANSFER', current_date - 6);
