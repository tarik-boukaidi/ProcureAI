TRUNCATE reception_lines, receptions RESTART IDENTITY CASCADE;

-- Receptions : 1 complete, 1 avec une chaise refusee, 1 partielle (15 ramettes sur 20), 1 complete
INSERT INTO receptions (purchase_order_id, supplier_id, delivery_note_number, received_date, comment) VALUES
  (1, 1, 'BL-ATL-5521', current_date - 17, NULL),
  (2, 2, 'BL-BP-0877', current_date - 13, 'Une chaise livrée avec le vérin cassé, refusée'),
  (3, 3, 'BL-MF-3310', current_date - 1, 'Livraison partielle, le reste est prévu la semaine prochaine'),
  (4, 1, 'BL-ATL-5398', current_date - 24, NULL);

INSERT INTO reception_lines (product_id, product_name, quantity_ordered, quantity_received, quantity_rejected, reception_id) VALUES
  (1, 'Écran Dell 27 pouces', 3, 3, 0, 1),
  (4, 'Chaise ergonomique Ergo Pro', 4, 4, 1, 2),
  (6, 'Ramette papier A4 (500 f.)', 20, 15, 0, 3),
  (3, 'Clavier Logitech MX Keys', 10, 10, 0, 4);
