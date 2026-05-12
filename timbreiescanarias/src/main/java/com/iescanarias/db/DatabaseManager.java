package com.iescanarias.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:timbre.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    public static void initialize() {
        String createTable = """
            CREATE TABLE IF NOT EXISTS songs (
                id       INTEGER PRIMARY KEY AUTOINCREMENT,
                title    TEXT    NOT NULL,
                filePath TEXT    NOT NULL UNIQUE,
                category TEXT    NOT NULL CHECK(category IN ('RECURRENTE','ESPECIAL','AMBIENTE')),
                active   INTEGER NOT NULL DEFAULT 1
            );
        """;

        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(createTable);
            System.out.println("[DB] Base de datos inicializada correctamente.");
        } catch (SQLException e) {
            System.err.println("[DB] Error al inicializar: " + e.getMessage());
        }
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error al cerrar conexión: " + e.getMessage());
        }
    }
}