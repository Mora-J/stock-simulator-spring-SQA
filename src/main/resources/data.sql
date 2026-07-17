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
   '$2a$10$7EqJtq98hPqEX7fNZaFWoOaJ2pO1pBwkFz8/9gWdfM2v0gNA5XO1u',
   NULL,
   false),
  ('Admin', 'User', 'usuario_admin', 'admin@example.com', true,
   '$2a$10$7EqJtq98hPqEX7fNZaFWoOaJ2pO1pBwkFz8/9gWdfM2v0gNA5XO1u',
   NULL,
   true);

INSERT INTO stock (ticker, name, description) VALUES
  ('MSFT', 'Microsoft', 'Microsoft Corporation stock'),
  ('AAPL', 'Apple', 'Apple Inc stock');

INSERT INTO owned_stock (ticker, name, quantity, stockuser_id) VALUES
  ('MSFT', 'Microsoft', 10, 1),
  ('AAPL', 'Apple', 15, 1),
  ('MSFT', 'Microsoft', 20, 2);