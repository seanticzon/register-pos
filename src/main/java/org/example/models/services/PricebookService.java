package org.example.models.services;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PricebookService {

    // Thread-safe cache for fast lookups
    private static final Map<String, Item> cacheById = new ConcurrentHashMap<>();
    private static final Map<String, String> cacheIdByName = new ConcurrentHashMap<>();

    /**
     * Loads the pricebook table from a TSV file into the database.
     * Call this after your DB is dropped/recreated.
     */
    public static void loadFromTSV() {
        try (InputStream in = PricebookService.class.getClassLoader().getResourceAsStream("pricebook.tsv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(in));
             Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO pricebook VALUES (?, ?, ?)")) {

            if (in == null) {
                throw new FileNotFoundException("pricebook.tsv not found in resources");
            }

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

    /**
     * Loads all products from the database into the cache for fast access.
     * Should be called right after loadFromTSV() or DB init.
     */
    public static void loadCacheFromDatabase() {
        cacheById.clear();
        cacheIdByName.clear();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, price FROM pricebook")) {

            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                BigDecimal price = rs.getBigDecimal("price");

                Item item = new Item(id, name, price);
                cacheById.put(id, item);
                cacheIdByName.put(name, id);
            }

            System.out.println("Pricebook cache loaded: " + cacheById.size() + " items.");

        } catch (Exception e) {
            System.out.println("Error loading cache: " + e.getMessage());
        }
    }

    /**
     * Gets an Item by ID from the cache.
     */
    public static Item getItemById(String id) {
        return cacheById.get(id);
    }

    /**
     * Gets all product names from the cache.
     */
    public static List<String> getAllProductNames() {
        return new ArrayList<>(cacheIdByName.keySet());
    }

    /**
     * Gets an optional ID by product name from the cache.
     */
    public static Optional<String> getIdByName(String name) {
        return Optional.ofNullable(cacheIdByName.get(name));
    }
}
