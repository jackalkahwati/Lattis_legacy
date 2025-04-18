CREATE TABLE `indiegogo_customer` (
  customer_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  perk_id INT,
  order_number INT,
  pledge_id INT,
  fulfillment_status VARCHAR(32),
  funding_date VARCHAR(64),
  payment_method VARCHAR(64),
  appearance VARCHAR(64),
  name VARCHAR(128),
  email VARCHAR(128),
  amount FLOAT,
  perk VARCHAR(64),
  first_name VARCHAR(64),
  last_name VARCHAR(64)
);
