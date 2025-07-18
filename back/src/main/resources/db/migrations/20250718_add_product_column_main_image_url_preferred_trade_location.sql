USE naver_market;

ALTER TABLE product ADD COLUMN main_image_url VARCHAR(255) NOT NULL;
ALTER TABLE product ADD COLUMN preferred_trade_location VARCHAR(100) NOT NULL;
