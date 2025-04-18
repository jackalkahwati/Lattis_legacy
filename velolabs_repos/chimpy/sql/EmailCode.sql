CREATE TABLE `email_code`(
  email_code_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  email_code CHAR(32),
  code INT UNSIGNED,
  email VARCHAR(128),
  new_email VARCHAR(128),
  first_name VARCHAR(64),
  last_name VARCHAR(64),
  has_updated TINYINT DEFAULT 0,
  quantity INT UNSIGNED,
  colors VARCHAR(1024),
  indiegogo_order_number INT UNSIGNED,
  shopify_customer_id INT UNSIGNED,
  stripe_customer_id VARCHAR(32)
);
