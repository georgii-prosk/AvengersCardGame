package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.repository.HeroRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Реализация {@link HeroRepository} на основе JDBC (PostgreSQL).
 */
public class PostgresHeroRepository implements HeroRepository {

    /** Запрос для поиска всех героев. */
    private static final String SQL_FIND_ALL = "SELECT * FROM heroes ORDER BY id";

    /** Запрос для поиска героя по ID. */
    private static final String SQL_FIND_BY_ID = "SELECT * FROM heroes WHERE id = ?";

    /** Запрос для поиска героев по редкости. */
    private static final String SQL_FIND_BY_RARITY = "SELECT * FROM heroes WHERE rarity = ? ORDER BY id";

    /** Запрос для вставки или обновления героя (UPSERT). */
    private static final String SQL_UPSERT = """
            INSERT INTO heroes (id, name, rarity, atk, def, hp, description)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                name = EXCLUDED.name,
                rarity = EXCLUDED.rarity,
                atk = EXCLUDED.atk,
                def = EXCLUDED.def,
                hp = EXCLUDED.hp,
                description = EXCLUDED.description
            """;

    /** Фабрика соединений. */
    private final ConnectionFactory connectionFactory;

    /**
     * @param connectionFactory фабрика соединений
     */
    public PostgresHeroRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<Hero> findAll() {
        List<Hero> heroes = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_FIND_ALL)) {
            while (rs.next()) {
                heroes.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all heroes", e);
        }
        return heroes;
    }

    @Override
    public Optional<Hero> findById(long id) {
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding hero by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Hero> findByRarity(HeroRarity rarity) {
        List<Hero> heroes = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_RARITY)) {
            stmt.setString(1, rarity.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    heroes.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding heroes by rarity", e);
        }
        return heroes;
    }

    @Override
    public void save(Hero hero) {
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPSERT)) {
            stmt.setLong(1, hero.id());
            stmt.setString(2, hero.name());
            stmt.setString(3, hero.rarity().name());
            stmt.setInt(4, hero.atk());
            stmt.setInt(5, hero.def());
            stmt.setInt(6, hero.hp());
            stmt.setString(7, hero.description());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving hero", e);
        }
    }

    /**
     * Преобразует строку ResultSet в объект героя.
     *
     * @param rs результат запроса
     * @return объект героя
     * @throws SQLException при ошибке чтения
     */
    private Hero mapRow(ResultSet rs) throws SQLException {
        return new Hero(
                rs.getLong("id"),
                rs.getString("name"),
                HeroRarity.valueOf(rs.getString("rarity")),
                rs.getInt("atk"),
                rs.getInt("def"),
                rs.getInt("hp"),
                rs.getString("description")
        );
    }
}