-- Коллекция карточек пользователей.
-- Справочник героев загружается из heroes.json.
CREATE TABLE IF NOT EXISTS user_cards (
    user_id BIGINT NOT NULL,
    hero_id BIGINT NOT NULL,
    count   INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (user_id, hero_id),
    FOREIGN KEY (user_id) REFERENCES users(telegram_id)
    );
