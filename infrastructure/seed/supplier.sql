-- Colonne ajoutee apres la creation de la table : on la cree si supplier-service n'a pas encore ete reconstruit
ALTER TABLE supplier ADD COLUMN IF NOT EXISTS ice varchar(255);

TRUNCATE product, supplier RESTART IDENTITY CASCADE;

-- Fournisseurs : ids 1 a 4 (ICE = 15 chiffres imprimes sur leurs factures)
INSERT INTO supplier (name, cin, ice, email, phone, address, city, status) VALUES
  ('Atlas Informatique', 'BK482913', '001525874000062', 'contact@atlas-info.ma', '0522 48 12 30', '112 Bd Zerktouni', 'Casablanca', 'ACTIVE'),
  ('Bureau Plus', 'AB209871', '002148563000041', 'commandes@bureauplus.ma', '0537 70 22 15', '18 Av. Mohammed V', 'Rabat', 'ACTIVE'),
  ('Maroc Fournitures', 'K394002', '000987412000078', 'ventes@marocfournitures.ma', '0539 94 31 08', 'Zone industrielle Gzenaya', 'Tanger', 'ACTIVE'),
  ('Sahara Logistique', 'EE118734', NULL, 'contact@saharalog.ma', '0524 43 67 90', 'Route de l''Ourika', 'Marrakech', 'INACTIVE');

-- Produits : ids 1 a 7 (utilises dans les commandes, receptions et factures)
INSERT INTO product (name, price, category, supplier_id) VALUES
  ('Écran Dell 27 pouces', 2490.00, 'IT', 1),
  ('PC portable Lenovo ThinkPad E14', 9800.00, 'IT', 1),
  ('Clavier Logitech MX Keys', 390.00, 'IT', 1),
  ('Chaise ergonomique Ergo Pro', 1450.00, 'FURNITURE', 2),
  ('Bureau 160 x 80 cm', 3200.00, 'FURNITURE', 2),
  ('Ramette papier A4 (500 f.)', 45.00, 'OFFICE_SUPPLIES', 3),
  ('Cartouche HP 305XL', 289.00, 'OFFICE_SUPPLIES', 3);
