package API;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dao.MenuItemDao;
import exception.InvalidInput;
import factory.MenuItemFactory;
import model.MenuItem;
import Service.MenuService;
import dao.RestaurantDao;
import model.Restaurant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


public class RestServer {
    private static final List<OrderDto> ORDERS = new ArrayList<>();
    private static int ORDER_SEQ = 1;

    private static class OrderDto {
        int id;
        int restaurantId;
        int itemsCount;
        double total;
        String createdAt;

        OrderDto(int id, int restaurantId, double total,int itemsCount, String createdAt) {
            this.id = id;
            this.restaurantId = restaurantId;
            this.total = total;
            this.itemsCount = itemsCount;
            this.createdAt = createdAt;
        }
    }

    public static void start() throws IOException {
        MenuService menuService = new MenuService(new MenuItemDao());
        RestaurantDao restaurantDao = new RestaurantDao();


        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);



        server.createContext("/restaurants", ex -> handleRestaurants(ex, menuService, restaurantDao));
        server.createContext("/menu-items", ex -> handleMenuItems(ex, menuService));
        server.createContext("/",RestServer::serveStatic);
        server.createContext("/orders", ex -> {
            try {
                if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                    String body = readBody(ex.getRequestBody());

                    int restaurantId = extractInt(body, "restaurantId");
                    double total = extractDouble(body, "total");

                    Map<Integer, Integer> qtyMap = extractItemsMap(body);

                    // ✅ ADD THIS LINE
                    int itemsCount = qtyMap.values().stream().mapToInt(Integer::intValue).sum();

                    List<MenuItem> menu = menuService.getMenu(restaurantId);

                    int id = ORDER_SEQ++;
                    ORDERS.add(new OrderDto(id, restaurantId, total, itemsCount, new Date().toString()));

                    String orderText = buildOrderText(id, menu, qtyMap, total);
                    String safe = escapeJsonString(orderText);

                    sendJson(ex, 201, "{\"status\":\"ok\",\"id\":" + id + ",\"text\":\"" + safe + "\"}");
                    return;
                }

                if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                    Integer restaurantId = extractQueryInt(ex.getRequestURI().getQuery(), "restaurantId");

                    List<OrderDto> filtered = ORDERS;
                    if (restaurantId != null) {
                        filtered = ORDERS.stream()
                                .filter(o -> o.restaurantId == restaurantId)
                                .collect(Collectors.toList());
                    }

                    sendJson(ex, 200, ordersToJson(filtered));
                    return;
                }

                sendText(ex, 405, "Method not allowed");
            } catch (Exception e) {
                sendText(ex, 400, "Bad request: " + e.getMessage());
            }
        });



        server.setExecutor(null);
        server.start();
        System.out.println("REST server started: http://localhost:8080");
    }

    private static void handleRestaurants(HttpExchange ex, MenuService menuService, RestaurantDao restaurantDao) throws IOException {
        try {
            String[] parts = ex.getRequestURI().getPath().split("/");
            if(parts.length == 2 && "restaurants".equals(parts[1]) && "GET".equalsIgnoreCase(ex.getRequestMethod())){
                List<Restaurant> list = restaurantDao.findAll();
                sendJson(ex ,200, restaurantsToJson(list));
            }
            if (parts.length == 2 && "restaurants".equals(parts[1]) && "POST".equalsIgnoreCase(ex.getRequestMethod())) {
                String body = readBody(ex.getRequestBody());
                String name = JSON.simpleString(body, "name");


                if (name == null || name.isBlank()) {
                    sendText(ex, 400, "Name cannot be empty");
                    return;
                }

                Integer existing = restaurantDao.findRestaurantIdByName(name);
                if (existing != null) {
                    sendText(ex, 409, "Restaurant already exists");
                    return;
                }

                int id = restaurantDao.createRestaurant(name.trim());
                sendJson(ex, 201, "{\"id\":" + id + "}");
                return;
            }
            if (parts.length == 4 && "restaurants".equals(parts[1]) && "menu".equals(parts[3])) {
                int restaurantId = Integer.parseInt(parts[2]);

                if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                    List<MenuItem> menu = menuService.getMenu(restaurantId);
                    sendJson(ex, 200, menuToJson(menu));
                    return;
                }

                if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                    String body = readBody(ex.getRequestBody());
                    String name = JSON.simpleString(body, "name");
                    double price = JSON.simpleDouble(body, "price");
                    String category = JSON.simpleString(body, "category");

                    MenuItem item = MenuItemFactory.create(category, name, price);
                    item.setRestaurantId(restaurantId);

                    int id = menuService.addItem(item);
                    sendJson(ex, 201, "{\"id\":" + id + "}");
                    return;
                }
            }

            sendText(ex, 404, "Not found");
        } catch (InvalidInput e) {
            sendText(ex, 400, e.getMessage());
        } catch (SQLException e) {
            sendText(ex, 500, "DB error");
        } catch (Exception e) {
            sendText(ex, 400, "Bad request");
        }
    }

    private static void handleMenuItems(HttpExchange ex, MenuService menuService) throws IOException {
        try {
            String[] parts = ex.getRequestURI().getPath().split("/");
            if (parts.length >= 3 && "menu-items".equals(parts[1])) {
                int itemId = Integer.parseInt(parts[2]);

                if (parts.length == 4 && "price".equals(parts[3]) && "PUT".equalsIgnoreCase(ex.getRequestMethod())) {
                    String body = readBody(ex.getRequestBody());
                    double price = JSON.simpleDouble(body, "price");
                    menuService.updatePrice(itemId, price);
                    sendJson(ex, 200, "{\"status\":\"ok\",\"message\":\"Price updated\"}");;
                    return;
                }

                if (parts.length == 3 && "DELETE".equalsIgnoreCase(ex.getRequestMethod())) {
                    menuService.deleteItem(itemId);
                    sendJson(ex, 200, "{\"status\":\"ok\",\"message\":\"Deleted\"}");
                    return;
                }
            }

            sendText(ex, 404, "Not found");
        } catch (InvalidInput e) {
            sendText(ex, 400, e.getMessage());
        } catch (SQLException e) {
            sendText(ex, 500, "DB error");
        } catch (Exception e) {
            sendText(ex, 400, "Bad request");
        }
    }

    private static String readBody(InputStream is) throws IOException {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void sendText(HttpExchange ex, int code, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private static void sendJson(HttpExchange ex, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private static String menuToJson(List<MenuItem> menu) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < menu.size(); i++) {
            MenuItem m = menu.get(i);
            sb.append("{")
                    .append("\"id\":").append(m.getId()).append(",")
                    .append("\"restaurantId\":").append(m.getRestaurantId()).append(",")
                    .append("\"name\":\"").append(escape(m.getName())).append("\",")
                    .append("\"price\":").append(m.getPrice()).append(",")
                    .append("\"category\":\"").append(escape(m.getCategory())).append("\"")
                    .append("}");
            if (i < menu.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }

    private static void serveStatic(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";

        Path filePath = Path.of("web" + path); // web/index.html, web/css/style.css

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            sendText(ex, 404, "Not found");
            return;
        }

        String ct = "text/plain; charset=utf-8";
        if (path.endsWith(".html")) ct = "text/html; charset=utf-8";
        else if (path.endsWith(".css")) ct = "text/css; charset=utf-8";
        else if (path.endsWith(".js")) ct = "application/javascript; charset=utf-8";
        else if (path.endsWith(".png")) ct = "image/png";
        else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) ct = "image/jpeg";
        else if (path.endsWith(".svg")) ct = "image/svg+xml";
        else if (path.endsWith(".ico")) ct = "image/x-icon";

        byte[] bytes = Files.readAllBytes(filePath);
        ex.getResponseHeaders().set("Content-Type", ct);
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }


    private static String restaurantsToJson(List<Restaurant> list){
        StringBuilder sb = new StringBuilder("[");
        for(int i = 0; i < list.size();i++){
            Restaurant r = list.get(i);
            sb.append("{")
                    .append("\"id\":").append(r.getId()).append(",")
                    .append("\"name\":\"").append(escape(r.getName())).append("\"")
                    .append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return  sb.toString();
    }

    private static String ordersToJson(List<OrderDto> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            OrderDto o = list.get(i);
            sb.append("{")
                    .append("\"id\":").append(o.id).append(",")
                    .append("\"restaurantId\":").append(o.restaurantId).append(",")
                    .append("\"total\":").append(o.total).append(",")
                    .append("\"itemsCount\":").append(o.itemsCount).append(",")
                    .append("\"createdAt\":\"").append(escape(o.createdAt)).append("\"")
                    .append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static Integer extractQueryInt(String query, String key) {
        if (query == null) return null;
        for (String part : query.split("&")) {
            String[] kv = part.split("=");
            if (kv.length == 2 && kv[0].equals(key)) return Integer.parseInt(kv[1]);
        }
        return null;
    }

    private static int extractInt(String json, String key) {
        int i = json.indexOf("\"" + key + "\"");
        if (i == -1) return 0;
        int c = json.indexOf(":", i);
        int end = json.indexOf(",", c);
        if (end == -1) end = json.indexOf("}", c);
        return Integer.parseInt(json.substring(c + 1, end).trim());
    }

    private static double extractDouble(String json, String key) {
        int i = json.indexOf("\"" + key + "\"");
        if (i == -1) return 0.0;
        int c = json.indexOf(":", i);
        int end = json.indexOf(",", c);
        if (end == -1) end = json.indexOf("}", c);
        return Double.parseDouble(json.substring(c + 1, end).trim());
    }
    private static String cap(String s) {
        if (s == null || s.isBlank()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private static Map<Integer, Integer> extractItemsMap(String json) {
        Map<Integer, Integer> map = new HashMap<>();
        int idx = 0;
        while (true) {
            int a = json.indexOf("\"menuItemId\"", idx);
            if (a == -1) break;

            int colon1 = json.indexOf(":", a);
            int comma1 = json.indexOf(",", colon1);
            int id = Integer.parseInt(json.substring(colon1 + 1, comma1).trim());

            int b = json.indexOf("\"quantity\"", comma1);
            int colon2 = json.indexOf(":", b);
            int end2 = json.indexOf("}", colon2);
            int qty = Integer.parseInt(json.substring(colon2 + 1, end2).trim());

            map.put(id, qty);
            idx = end2;
        }
        return map;
    }

    private static String buildOrderText(int orderId, List<MenuItem> menu, Map<Integer, Integer> qtyMap, double total) {
        StringBuilder sb = new StringBuilder();
        sb.append("Order #").append(orderId).append(":\n");

        for (MenuItem m : menu) {
            int q = qtyMap.getOrDefault(m.getId(), 0);
            if (q <= 0) continue;

            double line = m.getPrice() * q;
            sb.append(m.getName())
                    .append("[")
                    .append(cap(m.getCategory()))
                    .append("] - ")
                    .append(line)
                    .append("T\n");
        }

        sb.append("Total: ").append(total).append("T");
        return sb.toString();
    }

    private static String escapeJsonString(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

}
