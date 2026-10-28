ALTER TABLE user_cards
DROP CONSTRAINT IF EXISTS user_cards_hero_id_fkey;

DROP TABLE IF EXISTS heroes;
