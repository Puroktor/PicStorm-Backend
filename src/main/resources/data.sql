-- Password is "super-admin"
-- Ordinal of SUPER_ADMIN role is 3

INSERT INTO app_user (nickname, password_hash, email, role, created) VALUES
  ('Главный админ',  '$2a$10$4xyM5jI4.pCL7VjFmz0uNe6OJ6Z9YOvsqtjATPNN6IkRTeDevx0sO',
   'superadmin@gmail.com', 3, NOW()) ON CONFLICT DO NOTHING;