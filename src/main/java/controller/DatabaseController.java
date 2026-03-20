package controller;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
 import java.sql.Connection;
 import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;


public class DatabaseController {
    private static volatile boolean schemaInitialized = false;

    private final String url;
    private final String user;
    private final String password;

    public DatabaseController(String url, String user, String password) {
        if (isBlank(url)) {
            throw new IllegalArgumentException("Database URL must be provided");
        }
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static DatabaseController fromEnv() {
        String url = System.getenv("DATABASE_URL");
        String user = System.getenv("DATABASE_USER");
        String password = System.getenv("DATABASE_PASSWORD");
        if (isBlank(url)) {
            String host = System.getenv("PGHOST");
            String port = System.getenv("PGPORT");
            String database = System.getenv("PGDATABASE");
            if (isBlank(host)) {
                host = "localhost";
            }
            if (isBlank(port)) {
                port = "5432";
            }
            if (isBlank(database)) {
                database = "disaster_game";
            }
            url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        }
        if (isBlank(user)) {
            user = System.getenv("PGUSER");
        }
        if (isBlank(password)) {
            password = System.getenv("PGPASSWORD");
        }
        return new DatabaseController(url, user, password);
    }

    public Connection getConnection() throws SQLException {
        Connection connection;
        if (isBlank(user)) {
            try {
                connection = DriverManager.getConnection(url);
            } catch (SQLException ex) {
                connection = DriverManager.getConnection(url, "postgres", password);
            }
        } else {
            connection = DriverManager.getConnection(url, user, password);
        }
        ensureSchema(connection);
        return connection;
    }

    private void ensureSchema(Connection connection) {
        if (schemaInitialized) {
            return;
        }
        String schemaSql = readSchemaSql();
        if (schemaSql == null || schemaSql.isBlank()) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            for (String sql : schemaSql.split(";")) {
                String trimmed = sql.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                statement.execute(trimmed);
            }
            schemaInitialized = true;
        } catch (Exception ex) {
            System.err.println("Schema init failed: " + ex.getMessage());
        }
    }

    private String readSchemaSql() {
        try (InputStream input = DatabaseController.class.getResourceAsStream("/schema.sql")) {
            if (input == null) {
                return null;
            }
            try (Scanner scanner = new Scanner(input, StandardCharsets.UTF_8)) {
                scanner.useDelimiter("\\A");
                return scanner.hasNext() ? scanner.next() : "";
            }
        } catch (Exception ex) {
            System.err.println("Failed to read schema.sql: " + ex.getMessage());
            return null;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
