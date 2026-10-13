-- Hibernate (ddl-auto=update) ne met pas a jour la contrainte sur role :
-- une base creee avant l'ajout d'un role doit etre corrigee une fois.
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('ADMIN', 'MANAGER', 'EMPLOYEE', 'COMPTABLE', 'MAGASINIER'));

-- Comptes de test en plus de tarik@company.com (ADMIN, cree par DataSeeder).
-- Mots de passe : sara123, karim123, hamid123 et amina123 (hash BCrypt, comme BCryptPasswordEncoder).
INSERT INTO users (name, email, password, role, phone, address) VALUES
  ('Sara', 'sara@company.com', '$2a$10$mJyTYw/OuOS4.0eZHQBsLeDnXSbsx99QZ0mPjuhjdsJV9k34MfEOW', 'EMPLOYEE', '0661 22 33 44', 'Casablanca'),
  ('Karim', 'karim@company.com', '$2a$10$X.E8qgEjrtQtP0N3YENM9.4kKDqUxK6SApKkg4oxNE/V0kjkTnXxq', 'MANAGER', '0662 55 66 77', 'Casablanca'),
  ('Hamid', 'hamid@company.com', '$2a$10$9VmJgTii9p/zvQIbWMMNMuYe7GLvBm6FuDyyvyC1NTdy8qgogYkru', 'MAGASINIER', '0664 18 27 36', 'Casablanca'),
  ('Amina', 'amina@company.com', '$2a$10$s2okDI63KezSkYY7OCoQ0efCHA6gktsDGpjObuBq5lUrmp1r289Vy', 'COMPTABLE', '0665 40 51 62', 'Casablanca')
ON CONFLICT (email) DO NOTHING;
