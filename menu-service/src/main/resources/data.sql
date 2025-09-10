-- Insert categories
INSERT INTO categories (name, description) VALUES
('Pizza', 'Delicious handcrafted pizzas'),
('Sides', 'Perfect sides to complement your meal'),
('Beverages', 'Refreshing drinks and beverages'),
('Desserts', 'Sweet treats to end your meal');

-- Insert menu items
INSERT INTO menu_items (name, description, price, category_id, available, created_at, updated_at) VALUES
-- Pizzas
('Margherita', 'Classic pizza with tomato sauce, mozzarella, and basil', 12.99, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Pepperoni', 'Pizza with pepperoni and mozzarella cheese', 14.99, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Veggie Supreme', 'Pizza with bell peppers, onions, mushrooms, and olives', 16.99, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('BBQ Chicken', 'Pizza with BBQ chicken, red onions, and cilantro', 18.99, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Sides
('Garlic Bread', 'Fresh baked garlic bread', 6.99, 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Chicken Wings', '8 pieces of spicy chicken wings', 9.99, 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Caesar Salad', 'Fresh romaine lettuce with caesar dressing', 7.99, 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Beverages
('Coca Cola', 'Classic Coca Cola 330ml', 2.99, 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Orange Juice', 'Fresh orange juice 250ml', 3.99, 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Water', 'Bottled water 500ml', 1.99, 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Desserts
('Chocolate Brownie', 'Rich chocolate brownie with ice cream', 5.99, 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Tiramisu', 'Classic Italian tiramisu', 6.99, 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
