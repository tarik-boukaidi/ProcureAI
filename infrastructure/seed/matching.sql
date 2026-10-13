TRUNCATE match_discrepancies, match_results RESTART IDENTITY CASCADE;

-- Rapprochements deja faits (la facture 3 n'est pas encore rapprochee)
INSERT INTO match_results (invoice_id, purchase_order_id, status, checked_at) VALUES
  (1, 1, 'MATCHED', (current_date - 15) + time '10:20'),
  (2, 2, 'MISMATCH', (current_date - 11) + time '14:05'),
  (4, 4, 'MATCHED', (current_date - 22) + time '09:40');

-- Facture 2 : 4 chaises facturees, 3 seulement acceptees a la reception
INSERT INTO match_discrepancies (type, product_id, expected_value, actual_value, match_result_id) VALUES
  ('QUANTITY_EXCEEDS_RECEIVED', 4, '3', '4', 2);
