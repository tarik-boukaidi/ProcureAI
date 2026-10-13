TRUNCATE purchase_order_lines, purchase_orders RESTART IDENTITY CASCADE;

-- Commandes : ids 1 a 4, une par demande approuvee. Dates relatives a aujourd'hui.
-- La commande 3 est SENT (livraison partielle en cours) : on peut encore y ajouter une reception.
INSERT INTO purchase_orders (order_number, purchase_request_id, supplier_id, order_date, expected_delivery_date, total_amount, status) VALUES
  ('PO-A1F3C201', 1, 1, current_date - 28, current_date - 18, 7470.00, 'DELIVERED'),
  ('PO-B7D24E19', 2, 2, current_date - 24, current_date - 14, 5800.00, 'DELIVERED'),
  ('PO-C93E1A07', 3, 3, current_date - 12, current_date + 3, 900.00, 'SENT'),
  ('PO-D45B8F62', 4, 1, current_date - 35, current_date - 25, 3900.00, 'DELIVERED');

INSERT INTO purchase_order_lines (product_id, product_name, quantity, unit_price, purchase_order_id) VALUES
  (1, 'Écran Dell 27 pouces', 3, 2490.00, 1),
  (4, 'Chaise ergonomique Ergo Pro', 4, 1450.00, 2),
  (6, 'Ramette papier A4 (500 f.)', 20, 45.00, 3),
  (3, 'Clavier Logitech MX Keys', 10, 390.00, 4);
