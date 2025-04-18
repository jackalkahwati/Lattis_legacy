CREATE TABLE `address` (
  address_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  city VARCHAR(64),
  country VARCHAR(64),
  address1 VARCHAR(128),
  address2 VARCHAR(128),
  state VARCHAR(64),
  zip VARCHAR(64),
  phone VARCHAR(64),
  shopify_customer_id BIGINT(60) UNSIGNED,
  stripe_customer_id VARCHAR(32),
  stripe_card_id VARCHAR(32),
  indiegogo_customer_id INT
);
