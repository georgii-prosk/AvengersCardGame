ALTER TABLE users ADD COLUMN shards INTEGER NOT NULL DEFAULT 0;

ALTER TABLE users ADD CONSTRAINT users_balance_non_negative CHECK (balance >= 0);
ALTER TABLE users ADD CONSTRAINT users_shards_non_negative CHECK (shards >= 0);
ALTER TABLE user_cards ADD CONSTRAINT user_cards_count_non_negative CHECK (count >= 0);
