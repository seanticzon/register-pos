package org.example.models.services;

import org.example.models.services.Item;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PricebookService {

    public static void loadFromTSV() {
        try (InputStream in = PricebookService.class.getClassLoader().getResourceAsStream("pricebook.tsv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(in));
             Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO pricebook VALUES (?, ?, ?)")) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length == 3) {
                    stmt.setString(1, parts[0].trim());
                    stmt.setString(2, parts[1].trim());
                    stmt.setBigDecimal(3, new BigDecimal(parts[2].trim()));
                    stmt.executeUpdate();
                }
            }

        } catch (Exception e) {
            System.out.println("Error loading TSV: " + e.getMessage());
        }
    }

    public static Item getItemById(String id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name, price FROM pricebook WHERE id = ?")) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Item(id, rs.getString("name"), rs.getBigDecimal("price"));
            }

        } catch (Exception e) {
            System.out.println("Error fetching item by ID: " + e.getMessage());
        }
        return null;
    }

    public static List<String> getAllProductNames() {
        List<String> names = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM pricebook ORDER BY name")) {

            while (rs.next()) {
                names.add(rs.getString("name"));
            }

        } catch (Exception e) {
            System.out.println("Error fetching product names: " + e.getMessage());
        }
        return names;
    }

    public static Optional<String> getIdByName(String name) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM pricebook WHERE name = ?")) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getString("id"));
            }

        } catch (Exception e) {
            System.out.println("Error fetching ID by name: " + e.getMessage());
        }
        return Optional.empty();
    }
}
