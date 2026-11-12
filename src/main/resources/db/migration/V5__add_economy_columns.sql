ALTER TABLE users ADD COLUMN IF NOT EXISTS shards INTEGER NOT NULL DEFAULT 0;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'users_balance_non_negative' AND conrelid = 'users'::regclass
    ) THEN
        ALTER TABLE users ADD CONSTRAINT users_balance_non_negative CHECK (balance >= 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'users_shards_non_negative' AND conrelid = 'users'::regclass
    ) THEN
        ALTER TABLE users ADD CONSTRAINT users_shards_non_negative CHECK (shards >= 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'user_cards_count_non_negative' AND conrelid = 'user_cards'::regclass
    ) THEN
        ALTER TABLE user_cards ADD CONSTRAINT user_cards_count_non_negative CHECK (count >= 0);
    END IF;
END $$;
