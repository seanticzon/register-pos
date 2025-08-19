package org.example.models.services;

import java.sql.*;

public class DatabaseManager {
    public static final String JDBC_URL = "jdbc:h2:./database/testdb";
    public static final String USER = "sa";
    public static final String PASSWORD = "";

    public static void init() {
        startH2Console();
        setupDatabase();
    }

    private static void startH2Console() {
        try {
            org.h2.tools.Server.createWebServer("-webPort", "8082", "-browser").start();
            System.out.println("H2 Web Console started at: http://localhost:8082");
        } catch (Exception e) {
            System.out.println("H2 Console failed to start.");
        }
    }

    private static void setupDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");

            // Pricebook Table
            stmt.execute("""
                CREATE TABLE pricebook (
                    id VARCHAR(12) PRIMARY KEY,
                    name VARCHAR(255),
                    price DECIMAL(10,2)
                )
            """);

            // ✅ Insert system-reserved items for special journal actions
            //stmt.execute("INSERT INTO pricebook (id, name, price) VALUES ('SYS_PAYMENT', 'Payment Action', 0.00)");
            //stmt.execute("INSERT INTO pricebook (id, name, price) VALUES ('SYS_VOID_ALL', 'Void All Action', 0.00)");
            // Journal Table
            stmt.execute("""
                CREATE TABLE journal (
                    id IDENTITY PRIMARY KEY,
                    item_id VARCHAR(12),
                    item_qty INT,
                    action VARCHAR(255),
                    datetime TIMESTAMP,
                    FOREIGN KEY (item_id) REFERENCES pricebook(id)
                )
            """);

            // Receipts Table (1 row per item in the receipt)
            stmt.execute("""
                CREATE TABLE receipts (
                    id IDENTITY PRIMARY KEY,
                    receipt_id VARCHAR(36),
                    item_id VARCHAR(12),
                    qty INT,
                    unit_price DECIMAL(10, 2),
                    subtotal DECIMAL(10, 2),
                    amount_paid DECIMAL(10, 2),
                    change_due DECIMAL(10, 2),
                    created_at TIMESTAMP,
                    FOREIGN KEY (item_id) REFERENCES pricebook(id)
                )
            """);

            System.out.println("Database setup complete.");
        } catch (Exception e) {
            System.out.println("Database setup error: " + e.getMessage());
        }
     }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }
}
