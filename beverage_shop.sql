-- BEVERAGE DATABASE
-- CREATE QUERIES

CREATE TABLE users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(45) DEFAULT NULL,
  password VARCHAR(45) DEFAULT NULL,
  email VARCHAR(45) DEFAULT NULL,
  role ENUM('USER', 'SHOP_OWNER') DEFAULT 'USER' -- Default role is USER
);

CREATE TABLE beverages (
    beverage_id INT AUTO_INCREMENT PRIMARY KEY,
    beverage_name VARCHAR(255),
    beverage_cost DOUBLE,
    beverage_type ENUM('COFFEE', 'TEA', 'SOFT_DRINKS', 'FRESH_JUICE'),
    availability INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    modified_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    total_cost DOUBLE,
    order_status ENUM('PREPARING', 'SERVICED', 'DELIVERED'),
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

CREATE TABLE order_beverage (
    order_id INT,
    beverage_id INT,
	quantity INT,
    PRIMARY KEY (order_id, beverage_id) ON DELETE CASCADE
);

-----------------------------------------------------------------------------------
-- INSERT QUERIES

INSERT INTO users (user_id, created_at, email, password, username, role) VALUES (1, '2024-05-20 22:14:12.710439', 'john@gmail.com', '$2a$10$UzgM4cczXekCCQzJso2KI.8OPIiEOn0GlVJpO4w0jJy/MAr4Yz0h.', 'john_doe', 'USER');
INSERT INTO users (user_id, created_at, email, password, username, role) VALUES (2, '2024-05-20 22:14:12.813443', 'owner@admin.com', '$2a$10$Y9NtRgKZ18D35ZxNel99ze5D2eNh9Xag2Grji21ZccmDqJrlFQHOq', 'shop_owner', 'SHOP_OWNER');
INSERT INTO users (user_id, created_at, email, password, username, role) VALUES (3, '2024-05-20 22:14:12.892438', 'alice@gmail.com', '$2a$10$nTX.FJBTEJ1D1R91.qgdRebW.TRY6XljQGuo/zbTxhOWzK8ItOrbG', 'alice_smith', 'USER');

INSERT INTO `` (`beverage_id`,`availability`,`beverage_cost`,`beverage_name`,`beverage_type`,`created_at`,`modified_at`) VALUES (1,4,12.5,'Espresso','COFFEE','2024-05-20 22:15:25.172123',NULL);
INSERT INTO `` (`beverage_id`,`availability`,`beverage_cost`,`beverage_name`,`beverage_type`,`created_at`,`modified_at`) VALUES (2,8,11.8,'Green Tea','TEA','2024-05-20 22:15:25.172123',NULL);
INSERT INTO `` (`beverage_id`,`availability`,`beverage_cost`,`beverage_name`,`beverage_type`,`created_at`,`modified_at`) VALUES (3,5,13.5,'Cola','SOFT_DRINKS','2024-05-20 22:15:25.172123',NULL);
INSERT INTO `` (`beverage_id`,`availability`,`beverage_cost`,`beverage_name`,`beverage_type`,`created_at`,`modified_at`) VALUES (4,5,20,'Apple Juice','FRESH_JUICE','2024-05-20 22:15:25.172123',NULL);

INSERT INTO `` (`order_id`,`order_date`,`order_status`,`total_cost`,`user_id`) VALUES (1,'2024-05-20 22:20:31.834012','PREPARING',25,1);
INSERT INTO `` (`order_id`,`order_date`,`order_status`,`total_cost`,`user_id`) VALUES (2,'2024-05-15 11:30:00.000000','SERVED',15,3);
INSERT INTO `` (`order_id`,`order_date`,`order_status`,`total_cost`,`user_id`) VALUES (3,'2024-05-15 12:45:00.000000','SERVED',40.5,3);

INSERT INTO `` (`beverage_id`,`quantity`,`order_id`) VALUES (1,2,1);
INSERT INTO `` (`beverage_id`,`quantity`,`order_id`) VALUES (2,1,1);
INSERT INTO `` (`beverage_id`,`quantity`,`order_id`) VALUES (2,2,2);
INSERT INTO `` (`beverage_id`,`quantity`,`order_id`) VALUES (3,2,3);

------------------------------------------------------------------------------------
-- SELECT QUERIES

use beverage;
show tables;

select * from users;
select * from beverages;
update beverages set availability=4 where beverage_id=1;
select * from orders;
select * from order_beverage;
delete from orders where order_id=3;
