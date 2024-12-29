package com.wish.api;

import com.wish.API;
import com.wish.api.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class DatabaseManager {

    private final API plugin;
    private final DatabaseConnection database;
    private final DatabaseConnection connection;
    private final Map<UUID, Integer> cachedViolations;

    public DatabaseManager(API plugin) {
        this.plugin = plugin;
        this.database = new DatabaseConnection(plugin);
        this.connection = new DatabaseConnection(plugin);
        this.cachedViolations = new ConcurrentHashMap<>();
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            // Crear tablas
            try (Connection conn = connection.getConnection()) {
                // Tabla para alertas
                conn.createStatement().execute(
                        "CREATE TABLE IF NOT EXISTS alerts_status (" +
                                "player_uuid VARCHAR(36) PRIMARY KEY, " +
                                "alerts_enabled BOOLEAN DEFAULT false)"
                );

                // Tabla para violaciones
                conn.createStatement().execute(
                        "CREATE TABLE IF NOT EXISTS violations (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "player_uuid VARCHAR(36), " +
                                "check_name VARCHAR(50), " +
                                "vl INTEGER, " +
                                "timestamp BIGINT)"
                );

                plugin.getLogger().info("Base de datos " +
                        (plugin.getConfig().getString("database.type", "sqlite").equalsIgnoreCase("mysql") ? "MySQL" : "SQLite") +
                        " inicializada correctamente!");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al inicializar la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean updateAlertsStatus(UUID playerUUID, boolean enabled) {
        try (Connection conn = connection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT OR REPLACE INTO alerts_status (player_uuid, alerts_enabled) VALUES (?, ?)"
            );
            stmt.setString(1, playerUUID.toString());
            stmt.setBoolean(2, enabled);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al actualizar estado de alertas: " + e.getMessage());
            return false;
        }
    }

    public boolean getAlertsStatus(UUID playerUUID) {
        try (Connection conn = connection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT alerts_enabled FROM alerts_status WHERE player_uuid = ?"
            );
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getBoolean("alerts_enabled");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al obtener estado de alertas: " + e.getMessage());
            return false;
        }
    }

    public void addViolation(UUID playerUUID, String checkName) {
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO violations (uuid, check_name, violations) VALUES (?, ?, 1) "
                             + "ON DUPLICATE KEY UPDATE violations = violations + 1, last_violation = CURRENT_TIMESTAMP")) {

            ps.setString(1, playerUUID.toString());
            ps.setString(2, checkName);
            ps.executeUpdate();

            // Actualizar caché
            cachedViolations.merge(playerUUID, 1, Integer::sum);

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al añadir violación: " + e.getMessage());
        }
    }

    public int getViolations(UUID playerUUID) {
        return cachedViolations.computeIfAbsent(playerUUID, uuid -> {
            try (Connection conn = database.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT SUM(violations) FROM violations WHERE uuid = ?")) {

                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    return rs.getInt(1);
                }

            } catch (SQLException e) {
                plugin.getLogger().severe("Error al obtener violaciones: " + e.getMessage());
            }
            return 0;
        });
    }

    public void clearViolations(UUID playerUUID) {
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM violations WHERE uuid = ?")) {

            ps.setString(1, playerUUID.toString());
            ps.executeUpdate();

            // Limpiar caché
            cachedViolations.remove(playerUUID);

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al limpiar violaciones: " + e.getMessage());
        }
    }

    public void setAlertsEnabled(UUID playerUUID, boolean enabled) {
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO alerts_enabled (uuid, enabled) VALUES (?, ?) "
                             + "ON DUPLICATE KEY UPDATE enabled = ?")) {

            ps.setString(1, playerUUID.toString());
            ps.setBoolean(2, enabled);
            ps.setBoolean(3, enabled);
            ps.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al actualizar estado de alertas: " + e.getMessage());
        }
    }

    public boolean areAlertsEnabled(UUID playerUUID) {
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT enabled FROM alerts_enabled WHERE uuid = ?")) {

            ps.setString(1, playerUUID.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("enabled");
            }

            // Por defecto, las alertas están activadas
            return true;

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al obtener estado de alertas: " + e.getMessage());
            return true;
        }
    }

    public void shutdown() {
        database.close();
    }
}