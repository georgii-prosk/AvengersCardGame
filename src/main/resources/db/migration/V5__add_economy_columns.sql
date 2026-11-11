ALTER TABLE users ADD COLUMN IF NOT EXISTS shards INTEGER NOT NULL DEFAULT 0;

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_balance_non_negative;
ALTER TABLE users ADD CONSTRAINT users_balance_non_negative CHECK (balance >= 0);

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_shards_non_negative;
ALTER TABLE users ADD CONSTRAINT users_shards_non_negative CHECK (shards >= 0);

ALTER TABLE user_cards DROP CONSTRAINT IF EXISTS user_cards_count_non_negative;
ALTER TABLE user_cards ADD CONSTRAINT user_cards_count_non_negative CHECK (count >= 0);
