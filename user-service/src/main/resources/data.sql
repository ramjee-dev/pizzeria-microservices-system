-- Insert sample admin user
INSERT INTO users (username, email, password, first_name, last_name, phone, address, role, active, created_at, updated_at)
VALUES ('admin', 'admin@pizzeria.com', '$2a$10$EblZqNptyYdFMS7H7.QJ3uGvP4.dq8DJHj4JjdRD2ckQXL7g9Rg.S', 'Admin', 'User', '1234567890', 'Admin Address', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample customer user
INSERT INTO users (username, email, password, first_name, last_name, phone, address, role, active, created_at, updated_at)
VALUES ('customer1', 'customer1@example.com', '$2a$10$EblZqNptyYdFMS7H7.QJ3uGvP4.dq8DJHj4JjdRD2ckQXL7g9Rg.S', 'John', 'Doe', '9876543210', 'Customer Address', 'CUSTOMER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
