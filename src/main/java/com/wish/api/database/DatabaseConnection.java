package com.wish.api.database;

import com.wish.API;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

public class DatabaseConnection {
    private final API plugin;
    private Connection connection;
    private String type;
    private String connectionUrl;
    private String username;
    private String password;

    public DatabaseConnection(API plugin) {
        this.plugin = plugin;
        initializeConnectionSettings();
    }

    public void initializeConnectionSettings() {
        this.type = plugin.getConfig().getString("database.type", "sqlite");

        if (type.equalsIgnoreCase("mysql")) {
            String host = plugin.getConfig().getString("database.mysql.host");
            int port = plugin.getConfig().getInt("database.mysql.port");
            String database = plugin.getConfig().getString("database.mysql.database");
            this.username = plugin.getConfig().getString("database.mysql.username");
            this.password = plugin.getConfig().getString("database.mysql.password");

            // Obtener configuraciones avanzadas
            boolean useSSL = plugin.getConfig().getBoolean("database.mysql.advanced.useSSL", false);
            boolean allowPublicKeyRetrieval = plugin.getConfig().getBoolean("database.mysql.advanced.allowPublicKeyRetrieval", true);
            String timezone = plugin.getConfig().getString("database.mysql.advanced.serverTimezone", "UTC");

            this.connectionUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=%s&allowPublicKeyRetrieval=%s&serverTimezone=%s",
                    host, port, database, useSSL, allowPublicKeyRetrieval, timezone);
        } else {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists() && !dataFolder.mkdir()) {
                plugin.getLogger().severe("No se pudo crear el directorio de la base de datos");
                return;
            }
            this.connectionUrl = "jdbc:sqlite:" + new File(dataFolder, "database.db");
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }

        if (type.equalsIgnoreCase("mysql")) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL driver not found", e);
            }
            connection = DriverManager.getConnection(connectionUrl, username, password);
        } else {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite driver not found", e);
            }
            connection = DriverManager.getConnection(connectionUrl);
        }

        return connection;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                connection = null; // Importante: establecer a null después de cerrar
                plugin.getLogger().info("Conexión a la base de datos cerrada correctamente");
            } catch (SQLException e) {
                plugin.getLogger().severe("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }

    public void reload() {
        close();
        initializeConnectionSettings();
    }
}