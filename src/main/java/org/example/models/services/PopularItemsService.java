package org.example.models.services;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PopularItemsService {

    public static List<String> getTopPopularItemIds(int topN) {
        List<String> popularItemIds = new ArrayList<>();

        String sql = """
            SELECT item_id, SUM(qty) as total_qty
            FROM receipts
            GROUP BY item_id
            ORDER BY total_qty DESC
            LIMIT ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, topN);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                popularItemIds.add(rs.getString("item_id"));
            }

        } catch (Exception e) {
            System.out.println("Error fetching popular items: " + e.getMessage());
        }

        return popularItemIds;
    }

    public static List<Item> getPopularItems(int topN) {
        List<Item> popularItems = new ArrayList<>();
        List<String> popularIds = getTopPopularItemIds(topN);

        for (String id : popularIds) {
            Item item = PricebookService.getItemById(id);
            if (item != null) {
                popularItems.add(item);
            }
        }

        return popularItems;
    }
}
