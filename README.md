# register-pos

A simple desktop Point-of-Sale (POS) application built with Java Swing and Gradle. It demonstrates a modern POS workflow with a product grid, basket, discount application via HTTP services, journaling to a TCP server, receipt saving, and an embedded H2 database with a web console.

This README explains how to set up, run, and understand the structure of the app, what each class does, and how they work together.


## Quick start

- Prerequisites
  - Java 17 or newer (the code uses text blocks and modern HttpClient)
  - macOS/Windows/Linux
  - Optional: A discount service running locally if you want discount functionality
  - Optional: A TCP server to receive journal logs (defaults to localhost:1234)

- Run (recommended via IDE)
  1. Open the project in IntelliJ IDEA or your IDE of choice.
  2. Run the class: org.example.Main
  3. The app will maximize to your screen and preload a sample pricebook from resources/pricebook.tsv.

- Run H2 Web Console
  - The application automatically starts H2 web console on http://localhost:8082.
  - JDBC URL: jdbc:h2:./database/testdb
  - User: sa, Password: (empty)


## What you see in the UI

- Title bar (TitleBarPanel): shows app title, time, and a small connection widget to connect to a TCP server for journaling (host/port). It also offers a Journal popup window.
- Manual entry (ManualEntryPanel): type a product code and click Add (or press Enter) to add to basket.
- Product grid (ProductGridPanel): shows popular items (fetched from previous receipts) and lets you add with a click. Function keys are bound for quick actions.
- Basket (BasketPanel): the left panel with a table of scanned items, quantities, and prices. It shows subtotal, discount, tax, and total.
- Bottom bar (BottomBarPanel): payment actions. It can apply discounts via HTTP service(s), compute tax locally, accept payments (cash/credit), save receipts, and clear the basket.

Tip: Barcode scanners that type characters and hit Enter are supported globally. Scan anywhere; the app will capture the code and add it to the basket.


## High-level architecture

Startup sequence (org.example.Main):
1. DatabaseManager.init(): starts H2 web console and recreates tables.
2. JournalService.connectToServer(): initializes socket connection manager (you can connect later via the UI).
3. POSPanel is created on the Swing UI thread.

POSPanel responsibilities:
- On construction it loads the pricebook from resources/pricebook.tsv into the DB and warms up in-memory caches via PricebookService.
- Builds and wires the UI: TitleBarPanel, ManualEntryPanel, ProductGridPanel, BasketPanel, BottomBarPanel.
- Installs GlobalKeyScanner to capture barcode-like input anywhere and forwards scan events to BasketPanel.

Data flow:
- Scanning (GlobalKeyScanner → POSPanel.scanItem → BasketPanel.scanItem)
  - Looks up item in PricebookService cache and adds a row to the basket.
  - JournalService.log records the action in the journal table and optionally forwards a formatted line to the TCP server via SocketService.
- Discounts (BottomBarPanel)
  - Preferred: BasketPricingServiceClient sends subtotal and line items to a basket discount endpoint.
  - Fallback: DiscountServiceClient sends only the total to a simpler discount endpoint.
  - The result updates the displayed discount and is included in payment and receipt flows.
- Tax calculation (TaxCalculator): a simple 7% calculation done locally.
- Payment (BottomBarPanel → ReceiptService → ReceiptWindow): prompts for payment, logs details, saves receipt rows to the receipts table, shows a printable receipt view, then clears the basket.


## Key classes and what they do

- org.example.Main
  - Entry point. Initializes DB, sets up journaling lifecycle, and shows the main POS UI (POSPanel).

- org.example.models.services.POSPanel
  - Orchestrates app startup: loads pricebook TSV into DB, warms cache, builds Swing layout, installs GlobalKeyScanner, and wires panels.

- UI components (org.example.components)
  - TitleBarPanel: Compact header with title, clock, connection status; lets you set host/port and connect to a TCP journaling server; opens the Journal window.
  - ManualEntryPanel: Text field + button for typing/adding an item code manually.
  - ProductGridPanel: Shows popular items from receipts; clicking adds to basket; supports function-key shortcuts.
  - BasketPanel: Displays cart contents; computes subtotal/discount/tax/total; exposes helper methods for payment flow and journal logging.
  - BottomBarPanel: Houses payment buttons (Exact, Next Dollar, Custom). Applies discounts via HTTP, computes tax, prompts for payment, saves receipts, shows receipt window, and clears the basket.
  - JournalPanel: A simple table view over the journal table (item_id, qty, action, time) with Refresh.

- Services (org.example.models.services)
  - DatabaseManager: Starts H2 console, drops/recreates tables (pricebook, journal, receipts), and provides connections.
  - PricebookService: Loads products from resources/pricebook.tsv into DB; maintains thread-safe in-memory caches for fast lookup.
  - PopularItemsService: Queries receipts to determine popular item IDs and resolve them via PricebookService for the ProductGridPanel.
  - GlobalKeyScanner: Captures typing anywhere and on Enter treats it as a scanned barcode; forwards to POSPanel.
  - JournalService: Writes entries to journal table and forwards formatted log lines to the TCP server via SocketService. Provides connect/disconnect lifecycle helpers used by Main and TitleBarPanel.
  - SocketService: Simple singleton TCP client; configurable host/port; connects, sends lines, and disconnects.
  - BasketPricingServiceClient: HTTP client for posting full basket (items + subtotal) to a discount endpoint; parses a tolerant JSON response (no external JSON lib used).
  - DiscountServiceClient: Simpler HTTP client posting only total + discount name to a legacy discount endpoint.
  - ReceiptService: Persists a generated receipt (one row per basket line) to the receipts table with generated receipt_id.
  - ReceiptWindow: Shows a printable receipt UI with details and a Print button.

- Utilities
  - TaxCalculator: Central place for the tax rate (7%) and helper methods to compute tax and total with tax.

- Model
  - Item: Simple holder for pricebook entries (id, name, price).


## Database

- Embedded H2 database files live under ./database
- Tables are recreated on each startup by DatabaseManager:
  - pricebook(id, name, price)
  - journal(id, item_id, item_qty, action, datetime)
  - receipts(id, receipt_id, item_id, qty, unit_price, subtotal, amount_paid, change_due, created_at)
- Pricebook data is sourced from src/main/resources/pricebook.tsv (tab-separated: id, name, price)
- H2 Web Console: http://localhost:8082 (auto-start). Connect with JDBC URL jdbc:h2:./database/testdb, user sa, empty password.


## External services and configuration

Discount services
- Environment variables or JVM system properties can be used to configure endpoints.
- Basket-based endpoint (preferred):
  - Full URL override: env BASKET_PRICING_URL or -Dbasket.pricing.url
  - Else constructed from DISCOUNT_SERVICE_URL or -Ddiscount.service.url by appending /discount/applyBasket
  - Example request body (JSON):
    {
      "discountName": "SUMMER10",
      "subtotal": 34.50,
      "items": [
        {"id":"123","name":"Apple","qty":2,"unitPrice":2.50,"lineTotal":5.00}
      ]
    }
  - Expected response keys (any missing are tolerated):
    - originalSubtotal, discountName, discountPercentage, discountAmount, discountedSubtotal

- Legacy total-based endpoint:
  - Base URL: env DISCOUNT_SERVICE_URL or -Ddiscount.service.url (defaults to http://localhost:8080)
  - Path: /discount/apply
  - Example request body (JSON):
    {"total": 34.50, "discountName": "SUMMER10"}
  - Expected response keys:
    - originalTotal, discountName, discountPercentage, discountAmount, finalTotal

Journaling TCP server
- The top-right of the UI has fields for Host and Port and a Connect button.
- Defaults: localhost:1234
- Each journal entry is also saved to the DB regardless of TCP server connectivity.


## Building, running, and testing

- Build
  - ./gradlew clean build

- Run
  - Recommended: run org.example.Main from your IDE.
  - If you prefer command line, you can run using your IDE’s build or create a simple run configuration. The project does not include the Gradle application plugin, so gradle run is not configured by default.

- Tests
  - The project includes JUnit 5 dependencies, but no test classes currently.


## Barcode scanning and keyboard input

- GlobalKeyScanner captures key events application-wide. If characters are typed rapidly and Enter is pressed (as typical barcode scanners do), the accumulated code is sent to POSPanel.scanItem(..., source="Scanner").
- ManualEntryPanel also allows typing a code and clicking Add.


## Troubleshooting

- H2 console doesn’t open on 8082:
  - The port might be in use; the app will continue. You can still access the database files with any H2 client via jdbc:h2:./database/testdb.

- No products in product grid:
  - PopularItemsService derives items from past receipts; place sales first, or rely on Manual Entry and the Basket to start generating receipts.

- Discount not applied:
  - Ensure your discount service is running and reachable.
  - Set DISCOUNT_SERVICE_URL or BASKET_PRICING_URL (or the -D equivalents) before starting the app.
  - The app falls back from basket-based to legacy endpoint automatically if the basket endpoint fails.

- Journal TCP server:
  - Use the Title bar fields to set host/port and click Connect.
  - Even if not connected, journal entries are stored in the local DB.


## Project layout

- src/main/java
  - org.example.Main (entry point)
  - org.example.components.* (Swing UI panels)
  - org.example.models.services.* (database, networking, services, clients)
  - org.example.utils.* (utilities)
- src/main/resources
  - hibernate.cfg.xml
  - images/*
  - pricebook.tsv (sample catalog)
- database/* (H2 data files at runtime)


## License

This project is intended as a learning/demo POS application. No specific license set; adapt as needed.
