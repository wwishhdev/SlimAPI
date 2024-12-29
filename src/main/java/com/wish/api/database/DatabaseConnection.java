package com.wish.api.database;

import com.wish.API;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

public class DatabaseConnection {
    private final API plugin;
    private Connection connection;
    private final String type;

    public DatabaseConnection(API plugin) {
        this.plugin = plugin;
        this.type = plugin.getConfig().getString("database.type", "sqlite");
    }

    public Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }

        if (type.equalsIgnoreCase("mysql")) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                plugin.getLogger().severe("MySQL driver not found: " + e.getMessage());
                throw new SQLException("MySQL driver not found");
            }

            String host = plugin.getConfig().getString("database.mysql.host");
            int port = plugin.getConfig().getInt("database.mysql.port");
            String database = plugin.getConfig().getString("database.mysql.database");
            String username = plugin.getConfig().getString("database.mysql.username");
            String password = plugin.getConfig().getString("database.mysql.password");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
            connection = DriverManager.getConnection(url, username, password);
        } else {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                plugin.getLogger().severe("SQLite driver not found: " + e.getMessage());
                throw new SQLException("SQLite driver not found");
            }

            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdir();
            }

            String url = "jdbc:sqlite:" + new File(dataFolder, "database.db");
            connection = DriverManager.getConnection(url);
        }

        return connection;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}
