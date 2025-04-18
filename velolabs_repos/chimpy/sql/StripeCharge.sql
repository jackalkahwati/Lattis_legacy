CREATE TABLE `stripe_charge` (
  charge_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  stripe_charge_id VARCHAR(32),
  amount INT UNSIGNED,
  amount_refunded INT UNSIGNED,
  balance_transaction VARCHAR(64),
  created BIGINT UNSIGNED,
  currency VARCHAR(8),
  stripe_customer_id VARCHAR(32),
  description VARCHAR(128),
  object VARCHAR(32),
  paid TINYINT,
  receipt_email VARCHAR(64),
  receipt_number VARCHAR(64),
  refunded TINYINT,
  stripe_card_id VARCHAR(64),
  status VARCHAR(32)
);
