TRUNCATE TABLE transactions, owned_stock, stockuser, stock RESTART IDENTITY CASCADE;

INSERT INTO stockuser (
  first_name,
  last_name,
  username,
  email,
  verified,
  hashed_password,
  confirmation_code,
  admin
) VALUES
  ('Juan', 'Perez', 'investor01', 'investor01@example.com', true,
   '$2a$10$lY4roN4JKXV0jfMFV7FyteUJQVWp/BOzE69Yf5oChtVcS/BxWddAC',
   NULL,
   false),
  ('Admin', 'User', 'usuario_admin', 'admin@example.com', true,
   '$2a$10$lY4roN4JKXV0jfMFV7FyteUJQVWp/BOzE69Yf5oChtVcS/BxWddAC',
   NULL,
   true),
  ('Miguel', 'Gomez', 'unverified01', 'unverified01@example.com', false,
   '$2a$10$lY4roN4JKXV0jfMFV7FyteUJQVWp/BOzE69Yf5oChtVcS/BxWddAC',
   'pending-confirmation',
   false),
  ('Carlos', 'Ramirez', 'investor02', 'verified02@example.com', true,
   '$2a$10$lY4roN4JKXV0jfMFV7FyteUJQVWp/BOzE69Yf5oChtVcS/BxWddAC',
   NULL,
   false);

INSERT INTO stock (ticker, name, description) VALUES
  ('MSFT', 'Microsoft', 'Microsoft Corporation stock'),
  ('AAPL', 'Apple', 'Apple Inc stock');

INSERT INTO owned_stock (ticker, name, quantity, stockuser_id) VALUES
  ('MSFT', 'Microsoft', 10, 1),
  ('AAPL', 'Apple', 15, 1),
  ('MSFT', 'Microsoft', 20, 2);