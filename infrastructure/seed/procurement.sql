TRUNCATE purchase_request_lines, purchase_requests RESTART IDENTITY CASCADE;

-- Demandes : ids 1 a 7. :sara_id, :karim_id et :tarik_id sont passes par seed.ps1
INSERT INTO purchase_requests (reason, requester_id, status) VALUES
  ('Nouveaux postes pour l''équipe comptabilité', :sara_id, 'APPROVED'),
  ('Remplacement des chaises de l''open space', :sara_id, 'APPROVED'),
  ('Stock de papier du trimestre', :tarik_id, 'APPROVED'),
  ('Équipement des nouveaux arrivants', :sara_id, 'APPROVED'),
  ('Deux stagiaires arrivent le mois prochain', :sara_id, 'PENDING'),
  ('Imprimante du 2e étage', :sara_id, 'REJECTED'),
  ('Aménagement de la salle de réunion', :karim_id, 'PENDING');

INSERT INTO purchase_request_lines (product, quantity, purchase_request_id) VALUES
  ('Écran Dell 27 pouces', 3, 1),
  ('Chaise ergonomique Ergo Pro', 4, 2),
  ('Ramette papier A4 (500 f.)', 20, 3),
  ('Clavier Logitech MX Keys', 10, 4),
  ('PC portable Lenovo ThinkPad E14', 2, 5),
  ('Cartouche HP 305XL', 5, 6),
  ('Bureau 160 x 80 cm', 2, 7);
