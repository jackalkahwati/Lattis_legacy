CREATE TABLE `stripe_order` (
  order_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  amount FLOAT,
  amount_returned FLOAT,
  created BIGINT UNSIGNED,
  currency VARCHAR(16)
);
