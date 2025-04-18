create table `stripe_customer` (
  customer_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  stripe_customer_id VARCHAR(32),
  account_balance INT,
  created VARCHAR(32),
  default_source VARCHAR(32),
  description VARCHAR(64),
  email VARCHAR(64)
);
