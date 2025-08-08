package org.example.models.services;

import java.sql.*;

public class JournalService {

    public void log(String itemId, int qty, String action) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO journal (item_id, item_qty, action, datetime) VALUES (?, ?, ?, CURRENT_TIMESTAMP)"
             )) {

            stmt.setString(1, itemId);
            stmt.setInt(2, qty);
            stmt.setString(3, action);
            stmt.executeUpdate();

            System.out.printf("[Journal] Logged → ItemID: %s | Qty: %d | Action: %s%n", itemId, qty, action);

        } catch (Exception e) {
            System.err.println("Journal log error:");
            e.printStackTrace();
        }
    }

    // Optional: Console output
    public void printAllLogs() {
        try (Connection conn = DatabaseManager.getConnection()) {
            String logs = getAllLogs();
            System.out.println("===== All Journal Entries =====");
            System.out.print(logs);
            System.out.println("================================");
        } catch (Exception e) {
            System.err.println("Error printing all journal entries:");
            e.printStackTrace();
        }
    }

    // NEW: Returns all logs as a formatted string
    public String getAllLogs() {
        StringBuilder sb = new StringBuilder();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM journal ORDER BY datetime DESC")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String itemId = rs.getString("item_id");
                int qty = rs.getInt("item_qty");
                String action = rs.getString("action");
                Timestamp datetime = rs.getTimestamp("datetime");

                sb.append(String.format("ID: %d | Item: %s | Qty: %d | Action: %s | Time: %s%n",
                        id, itemId, qty, action, datetime));
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving journal logs:");
            e.printStackTrace();
        }

        return sb.toString();
    }

    public void clearLog() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM journal");
            System.out.println("[Journal] All entries cleared.");
        } catch (Exception e) {
            System.err.println("Journal clear error:");
            e.printStackTrace();
        }
    }
}
