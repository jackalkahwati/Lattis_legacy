CREATE TABLE `shopify_customer` (
  customer_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  shopify_customer_id BIGINT(60) UNSIGNED,
  state VARCHAR(16),
  default_address_id BIGINT(20) UNSIGNED,
  first_name VARCHAR(64),
  last_name VARCHAR(64),
  accepts_marketing TINYINT,
  total_spent FLOAT,
  email VARCHAR(64),
  created_at VARCHAR(32),
  update_at VARCHAR(32),
  orders_count INT UNSIGNED,
  last_order_id BIGINT UNSIGNED,
  last_order_name VARCHAR(32)
);
