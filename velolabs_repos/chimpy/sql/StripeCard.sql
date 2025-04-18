CREATE TABLE `stripe_card` (
  card_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  stripe_card_id VARCHAR(32),
  brand VARCHAR(16),
  country VARCHAR(3),
  stripe_customer_id VARCHAR(24),
  exp_month VARCHAR(2),
  exp_year CHAR(4),
  fingerprint VARCHAR(24),
  funding VARCHAR(10),
  last4 CHAR(4),
  name varchar(64),
  object varchar(16)
);
