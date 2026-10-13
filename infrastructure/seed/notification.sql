-- Hibernate (ddl-auto=update) ne met pas a jour la contrainte sur type :
-- une base creee avant l'ajout d'un type doit etre corrigee une fois.
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check;
ALTER TABLE notifications ADD CONSTRAINT notifications_type_check CHECK (type IN (
  'DEMANDE_CREATED', 'DEMANDE_APPROVED', 'DEMANDE_REJECTED', 'INVOICE_REJECTED', 'INVOICE_PAID', 'ORDER_SENT'));

TRUNCATE notifications RESTART IDENTITY CASCADE;

-- Memes messages que ceux envoyes par les services. :sara_id et :tarik_id sont passes par seed.ps1
INSERT INTO notifications (recipient_user_id, recipient_role, type, message, is_read, created_at) VALUES
  (:sara_id, NULL, 'DEMANDE_APPROVED', 'Votre demande #1 est approuvee', true, (current_date - 29) + time '11:02'),
  (:tarik_id, NULL, 'DEMANDE_APPROVED', 'Votre demande #3 est approuvee', true, (current_date - 13) + time '09:15'),
  (NULL, 'COMPTABLE', 'INVOICE_REJECTED', 'Facture BP-2026-0412 rejetee', false, (current_date - 11) + time '14:05'),
  (NULL, 'COMPTABLE', 'INVOICE_PAID', 'Facture FA-ATL-2026-118 payee : 7470.00', true, (current_date - 6) + time '16:30'),
  (:sara_id, NULL, 'DEMANDE_REJECTED', 'Votre demande #6 est rejetee', false, (current_date - 4) + time '10:48'),
  (NULL, 'MANAGER', 'DEMANDE_CREATED', 'Nouvelle demande #5 a approuver', false, (current_date - 2) + time '08:57'),
  (NULL, 'MANAGER', 'DEMANDE_CREATED', 'Nouvelle demande #7 a approuver', false, current_date + time '09:12');
