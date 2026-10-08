CREATE TABLE users (
                       telegram_id   BIGINT PRIMARY KEY,
                       display_name  VARCHAR(128) NOT NULL,
                       bio           VARCHAR(256),
                       balance       INTEGER DEFAULT 0,
                       card_count    INTEGER DEFAULT 0,
                       created_at    TIMESTAMP DEFAULT NOW(),
                       updated_at    TIMESTAMP DEFAULT NOW()
);