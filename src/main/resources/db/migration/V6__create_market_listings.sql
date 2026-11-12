CREATE TABLE IF NOT EXISTS market_listings (
    id         BIGSERIAL PRIMARY KEY,
    seller_id  BIGINT NOT NULL,
    hero_id    BIGINT NOT NULL,
    price      INTEGER NOT NULL CHECK (price > 0),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (seller_id) REFERENCES users(telegram_id)
    );

CREATE INDEX IF NOT EXISTS idx_market_listings_seller ON market_listings(seller_id);
