package org.example.models.services;

import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JournalService {

    private static final SocketService socketService = SocketService.getInstance();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Updated log method with socket communication
    public static void log(String itemId, int qty, String action) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO journal (item_id, item_qty, action, datetime) VALUES (?, ?, ?, CURRENT_TIMESTAMP)"
             )) {

            stmt.setString(1, itemId);
            stmt.setInt(2, qty);
            stmt.setString(3, action);
            stmt.executeUpdate();

            // Create formatted log message for server
            String timestamp = LocalDateTime.now().format(formatter);
            String logMessage = String.format("[%s] ItemID: %s | Qty: %d | Action: %s", 
                                             timestamp, itemId, qty, action);

            System.out.printf("[Journal] Logged → ItemID: %s | Qty: %d | Action: %s%n", itemId, qty, action);

            // Send to server
            sendLogToServer(logMessage);

        } catch (Exception e) {
            System.err.println("Journal log error:");
            e.printStackTrace();
        }
    }

    // New overloaded log method that accepts tax, appends tax info to action string
    public static void log(String itemId, int qty, String action, BigDecimal tax) {
        // Append tax info to action
        String actionWithTax = String.format("%s | Tax: $%.2f", action, tax);

        // Call existing log method
        log(itemId, qty, actionWithTax);
    }

    private static void sendLogToServer(String logMessage) {
        try {
            socketService.sendLog(logMessage);
        } catch (Exception e) {
            System.err.println("[Journal] Failed to send log to server: " + e.getMessage());
        }
    }

    // Method to manually connect to server (call this during startup)
    public static void connectToServer() {
        socketService.connect();
    }

    // Method to disconnect from server (call this during shutdown)
    public static void disconnectFromServer() {
        socketService.disconnect();
    }


//    // Optional: Console output
//    public static void printAllLogs() {
//        try (Connection conn = DatabaseManager.getConnection()) {
//            String logs = getAllLogs();
//            System.out.println("===== All Journal Entries =====");
//            System.out.print(logs);
//            System.out.println("================================");
//        } catch (Exception e) {
//            System.err.println("Error printing all journal entries:");
//            e.printStackTrace();
//        }
//    }

    // Returns all logs as a formatted string
//    public static String getAllLogs() {
//        StringBuilder sb = new StringBuilder();
//
//        try (Connection conn = DatabaseManager.getConnection();
//             Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery("SELECT * FROM journal ORDER BY datetime DESC")) {
//
//            while (rs.next()) {
//                int id = rs.getInt("id");
//                String itemId = rs.getString("item_id");
//                int qty = rs.getInt("item_qty");
//                String action = rs.getString("action");
//                Timestamp datetime = rs.getTimestamp("datetime");
//
//                sb.append(String.format("ID: %d | Item: %s | Qty: %d | Action: %s | Time: %s%n",
//                        id, itemId, qty, action, datetime));
//            }
//
//        } catch (SQLException e) {
//            System.err.println("Error retrieving journal logs:");
//            e.printStackTrace();
//        }
//
//        return sb.toString();
//    }

//    public static void clearLog() {
//        try (Connection conn = DatabaseManager.getConnection();
//             Statement stmt = conn.createStatement()) {
//            stmt.executeUpdate("DELETE FROM journal");
//            System.out.println("[Journal] All entries cleared.");
//        } catch (Exception e) {
//            System.err.println("Journal clear error:");
//            e.printStackTrace();
//        }
//    }
}
