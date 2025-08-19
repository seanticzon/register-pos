package org.example.models.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Unified discount service client that handles both basket-based and simple total-based discounts.
 * Replaces the deprecated DiscountServiceClient with enhanced functionality.
 */
public class BasketPricingServiceClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    public static class LineItem {
        public String id;
        public String name;
        public int qty;
        public double unitPrice;
        public double lineTotal;
    }

    public static class BasketResult {
        public double originalSubtotal;     // subtotal before discount
        public String discountName;         // echo or resolved name
        public double discountPercentage;   // optional from server
        public double discountAmount;       // amount discounted
        public double discountedSubtotal;   // subtotal after discount
    }

    /**
     * Resolves base URL from:
     * 1) System property: basket.pricing.url (full URL, wins if present)
     * 2) Env var: BASKET_PRICING_URL (full URL)
     * 3) Otherwise builds from DISCOUNT_SERVICE_URL + "/discount/applyBasket"
     * 4) Otherwise defaults to http://localhost:8080/discount/applyBasket
     */
    private static String resolveUrl() {
        // Full URL overrides
        String fullProp = System.getProperty("basket.pricing.url");
        if (fullProp != null && !fullProp.isBlank()) return fullProp.trim();
        String fullEnv = System.getenv("BASKET_PRICING_URL");
        if (fullEnv != null && !fullEnv.isBlank()) return fullEnv.trim();

        // Build from discount service base
        String base = System.getProperty("discount.service.url");
        if (base == null || base.isBlank()) base = System.getenv("DISCOUNT_SERVICE_URL");
        if (base == null || base.isBlank()) base = "http://LB-DS-8080-2-529779997.us-east-1.elb.amazonaws.com:8080"; // Change this to your Discount EC2 Endpoint
        return (base.endsWith("/") ? base + "discount/applyBasket" : base + "/discount/applyBasket");
    }

    /**
     * Applies discount to a full basket with detailed line items.
     */
    public BasketResult applyDiscountToBasket(List<LineItem> items, double subtotal, String discountName) throws Exception {
        String jsonBody = buildRequestJson(items, subtotal, discountName);
        String url = resolveUrl();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        String body = response.body() == null ? "" : response.body();
        if (status < 200 || status >= 300) {
            throw new RuntimeException("Basket pricing service error: HTTP " + status + " - " + body);
        }
        return parseBasketResponse(body);
    }

    /**
     * Simplified discount method that creates a single line item from the total.
     * This replaces the functionality of DiscountServiceClient for backward compatibility.
     */
    public BasketResult applySimpleDiscount(double total, String discountName) throws Exception {
        // Create a single line item representing the total
        List<LineItem> items = new ArrayList<>();
        LineItem singleItem = new LineItem();
        singleItem.id = "TOTAL";
        singleItem.name = "Order Total";
        singleItem.qty = 1;
        singleItem.unitPrice = total;
        singleItem.lineTotal = total;
        items.add(singleItem);

        return applyDiscountToBasket(items, total, discountName);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String buildRequestJson(List<LineItem> items, double subtotal, String discountName) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"discountName\":\"").append(escapeJson(discountName == null ? "" : discountName)).append("\",");
        sb.append("\"subtotal\":").append(String.format(java.util.Locale.US, "%.2f", subtotal)).append(",");
        sb.append("\"items\":[");
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                LineItem it = items.get(i);
                if (i > 0) sb.append(',');
                sb.append('{')
                        .append("\"id\":\"").append(escapeJson(it.id)).append("\",")
                        .append("\"name\":\"").append(escapeJson(it.name)).append("\",")
                        .append("\"qty\":").append(it.qty).append(',')
                        .append("\"unitPrice\":").append(String.format(java.util.Locale.US, "%.2f", it.unitPrice)).append(',')
                        .append("\"lineTotal\":").append(String.format(java.util.Locale.US, "%.2f", it.lineTotal))
                        .append('}')
                ;
            }
        }
        sb.append(']');
        sb.append('}');
        return sb.toString();
    }

    private BasketResult parseBasketResponse(String json) {
        BasketResult r = new BasketResult();
        r.originalSubtotal = extractDouble(json, "originalSubtotal");
        if (r.originalSubtotal == 0.0) {
            // try alternate key name
            r.originalSubtotal = extractDouble(json, "subtotal");
        }
        r.discountName = extractString(json, "discountName");
        r.discountPercentage = extractDouble(json, "discountPercentage");
        r.discountAmount = extractDouble(json, "discountAmount");
        r.discountedSubtotal = extractDouble(json, "discountedSubtotal");
        if (r.discountedSubtotal == 0.0 && r.originalSubtotal > 0) {
            // compute if server only returned discountAmount
            r.discountedSubtotal = Math.max(0.0, r.originalSubtotal - r.discountAmount);
        }
        return r;
    }

    // Parsing helpers (simple, tolerant)
    private static int indexOfKey(String json, String key) {
        String needle = "\"" + key + "\"";
        return json.indexOf(needle);
    }

    private static double extractDouble(String json, String key) {
        int keyPos = indexOfKey(json, key);
        if (keyPos < 0) return 0.0;
        int colon = json.indexOf(':', keyPos);
        if (colon < 0) return 0.0;
        int i = colon + 1;
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
        boolean quoted = i < json.length() && json.charAt(i) == '"';
        if (quoted) i++;
        int start = i;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (quoted) {
                if (c == '"') break;
            } else {
                if (c == ',' || c == '}' || Character.isWhitespace(c)) break;
            }
            i++;
        }
        String token = json.substring(start, i).trim();
        if (token.startsWith("\"") && token.endsWith("\"") && token.length() >= 2) {
            token = token.substring(1, token.length() - 1);
        }
        try { return Double.parseDouble(token); } catch (Exception e) { return 0.0; }
    }

    private static String extractString(String json, String key) {
        int keyPos = indexOfKey(json, key);
        if (keyPos < 0) return null;
        int colon = json.indexOf(':', keyPos);
        if (colon < 0) return null;
        int i = colon + 1;
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
        if (i >= json.length()) return null;
        if (json.charAt(i) == '"') {
            int start = i + 1;
            int end = json.indexOf('"', start);
            if (end > start) return json.substring(start, end);
            return null;
        } else {
            int start = i;
            while (i < json.length()) {
                char c = json.charAt(i);
                if (c == ',' || c == '}' || Character.isWhitespace(c)) break;
                i++;
            }
            return json.substring(start, i).trim();
        }
    }

    // Utility to convert table model entries into LineItem list (optional helper if needed elsewhere)
    public static List<LineItem> fromTable(javax.swing.table.DefaultTableModel model) {
        List<LineItem> items = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            LineItem li = new LineItem();
            li.id = String.valueOf(model.getValueAt(i, 0));
            li.name = String.valueOf(model.getValueAt(i, 1));
            try { li.qty = Integer.parseInt(String.valueOf(model.getValueAt(i, 2))); } catch (Exception e) { li.qty = 1; }
            try { li.unitPrice = Double.parseDouble(String.valueOf(model.getValueAt(i, 3))); } catch (Exception e) { li.unitPrice = 0.0; }
            li.lineTotal = li.unitPrice * li.qty;
            items.add(li);
        }
        return items;
    }
}