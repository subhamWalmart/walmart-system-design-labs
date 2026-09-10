-- Sample product catalog / inventory so a test order can succeed out of the box.
-- JpaInventoryRepository queries this table by product name (name match is
-- case-sensitive - order Item Names must match these names exactly).
CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL
);

INSERT INTO product (name, price, quantity) VALUES ('Laptop', 999.99, 15);
INSERT INTO product (name, price, quantity) VALUES ('Mouse', 19.99, 100);
INSERT INTO product (name, price, quantity) VALUES ('Keyboard', 49.99, 60);
INSERT INTO product (name, price, quantity) VALUES ('Monitor', 199.99, 30);
INSERT INTO product (name, price, quantity) VALUES ('Headphones', 79.99, 45);
