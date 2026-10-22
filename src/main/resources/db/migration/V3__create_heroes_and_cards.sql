-- Справочник героев
CREATE TABLE IF NOT EXISTS heroes (
    id          BIGINT PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    rarity      VARCHAR(32) NOT NULL,
    atk         INTEGER NOT NULL,
    def         INTEGER NOT NULL,
    hp          INTEGER NOT NULL,
    description VARCHAR(512)
    );

-- Коллекция карточек пользователей
CREATE TABLE IF NOT EXISTS user_cards (
    user_id BIGINT NOT NULL,
    hero_id BIGINT NOT NULL,
    count   INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (user_id, hero_id),
    FOREIGN KEY (user_id) REFERENCES users(telegram_id),
    FOREIGN KEY (hero_id) REFERENCES heroes(id)
    );