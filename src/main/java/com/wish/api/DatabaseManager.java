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
    private DatabaseConnection connection;
    private boolean isMySQL;
    private final Map<UUID, Integer> cachedViolations;

    public DatabaseManager(API plugin) {
        this.plugin = plugin;
        this.cachedViolations = new ConcurrentHashMap<>();
        initialize();
    }

    private void initialize() {
        this.isMySQL = plugin.getConfig().getString("database.type", "sqlite").equalsIgnoreCase("mysql");
        this.connection = new DatabaseConnection(plugin);
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = connection.getConnection()) {
            if (isMySQL) {
                // Sintaxis MySQL
                conn.createStatement().execute(
                        "CREATE TABLE IF NOT EXISTS alerts_status (" +
                                "player_uuid VARCHAR(36) PRIMARY KEY, " +
                                "alerts_enabled BOOLEAN DEFAULT false) " +
                                "ENGINE=InnoDB"
                );

                conn.createStatement().execute(
                        "CREATE TABLE IF NOT EXISTS violations (" +
                                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                                "player_uuid VARCHAR(36), " +           // Cambiado de uuid a player_uuid
                                "check_name VARCHAR(50), " +
                                "vl INT, " +
                                "timestamp BIGINT, " +
                                "INDEX idx_player (player_uuid)) " +    // Actualizado el índice también
                                "ENGINE=InnoDB"
                );
            } else {
                // Sintaxis SQLite
                conn.createStatement().execute(
                        "CREATE TABLE IF NOT EXISTS alerts_status (" +
                                "player_uuid VARCHAR(36) PRIMARY KEY, " +
                                "alerts_enabled BOOLEAN DEFAULT 0)"
                );

                conn.createStatement().execute(
                        "CREATE TABLE IF NOT EXISTS violations (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "player_uuid VARCHAR(36), " +           // Cambiado de uuid a player_uuid
                                "check_name VARCHAR(50), " +
                                "vl INTEGER, " +
                                "timestamp INTEGER)"
                );
            }

            plugin.getLogger().info("Database " +
                    (isMySQL ? "MySQL" : "SQLite") +
                    " initialized successfully!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error initializing database: " + e.getMessage());
            if (isMySQL) {
                plugin.getLogger().severe("Current MySQL configuration:");
                plugin.getLogger().severe("Host: " + plugin.getConfig().getString("database.mysql.host"));
                plugin.getLogger().severe("Port: " + plugin.getConfig().getInt("database.mysql.port"));
                plugin.getLogger().severe("Database: " + plugin.getConfig().getString("database.mysql.database"));
                plugin.getLogger().severe("User: " + plugin.getConfig().getString("database.mysql.username"));
            }
            e.printStackTrace();
        }
    }

    public void reload() {
        try {
            // Cerrar la conexión existente
            if (connection != null) {
                connection.close();
            }

            // Reinicializar todo
            initialize();

            plugin.getLogger().info("Database reconfigured successfully");
        } catch (Exception e) {
            plugin.getLogger().severe("Error reloading database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean updateAlertsStatus(UUID playerUUID, boolean enabled) {
        try (Connection conn = connection.getConnection()) {
            String sql;
            if (isMySQL) {
                sql = "INSERT INTO alerts_status (player_uuid, alerts_enabled) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE alerts_enabled = VALUES(alerts_enabled)";
            } else {
                sql = "INSERT OR REPLACE INTO alerts_status (player_uuid, alerts_enabled) VALUES (?, ?)";
            }

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, playerUUID.toString());
            stmt.setBoolean(2, enabled);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Error updating alert status: " + e.getMessage());
            return false;
        }
    }

    public boolean testConnection() {
        try (Connection conn = connection.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error testing connection: " + e.getMessage());
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
            plugin.getLogger().severe("Error getting alert status: " + e.getMessage());
            return false;
        }
    }

    public void addViolation(UUID playerUUID, String checkName) {
        try (Connection conn = connection.getConnection()) {
            String sql;
            if (isMySQL) {
                sql = "INSERT INTO violations (player_uuid, check_name, vl, timestamp) VALUES (?, ?, 1, ?) " +
                        "ON DUPLICATE KEY UPDATE vl = vl + 1, timestamp = ?";
            } else {
                sql = "INSERT INTO violations (player_uuid, check_name, vl, timestamp) VALUES (?, ?, 1, ?)";
            }

            long timestamp = System.currentTimeMillis();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, playerUUID.toString());
            ps.setString(2, checkName);
            ps.setLong(3, timestamp);
            if (isMySQL) {
                ps.setLong(4, timestamp);
            }
            ps.executeUpdate();

            // Actualizar caché
            cachedViolations.merge(playerUUID, 1, Integer::sum);
        } catch (SQLException e) {
            plugin.getLogger().severe("Error adding violation: " + e.getMessage());
        }
    }

    public int getViolations(UUID playerUUID) {
        return cachedViolations.computeIfAbsent(playerUUID, uuid -> {
            try (Connection conn = connection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT SUM(vl) FROM violations WHERE player_uuid = ?")) { // Cambiado de uuid a player_uuid

                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error getting violations: " + e.getMessage());
            }
            return 0;
        });
    }

    public void clearViolations(UUID playerUUID) {
        try (Connection conn = connection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM violations WHERE player_uuid = ?"
            );
            stmt.setString(1, playerUUID.toString());
            stmt.executeUpdate();

            // Limpiar caché
            cachedViolations.remove(playerUUID);
        } catch (SQLException e) {
            plugin.getLogger().severe("Error clearing violations: " + e.getMessage());
        }
    }

    public void setAlertsEnabled(UUID playerUUID, boolean enabled) {
        try (Connection conn = connection.getConnection()) {
            String sql;
            if (isMySQL) {
                sql = "INSERT INTO alerts_status (player_uuid, alerts_enabled) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE alerts_enabled = ?";
            } else {
                sql = "INSERT OR REPLACE INTO alerts_status (player_uuid, alerts_enabled) VALUES (?, ?)";
            }

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, playerUUID.toString());
            ps.setBoolean(2, enabled);
            if (isMySQL) {
                ps.setBoolean(3, enabled);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error updating alert status: " + e.getMessage());
        }
    }

    public boolean areAlertsEnabled(UUID playerUUID) {
        try (Connection conn = connection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT alerts_enabled FROM alerts_status WHERE player_uuid = ?"
            );
            ps.setString(1, playerUUID.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("alerts_enabled");
            }

            // Por defecto, las alertas están activadas
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting alert status: " + e.getMessage());
            return true;
        }
    }

    public void shutdown() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Database connection closed successfully.");
            } catch (Exception e) {
                plugin.getLogger().warning("Error closing database connection: " + e.getMessage());
            }
        }
    }
}