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

public class RestServer {

    public static void start() throws IOException {
        MenuService menuService = new MenuService(new MenuItemDao());
        RestaurantDao restaurantDao = new RestaurantDao();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/restaurants", ex -> handleRestaurants(ex, menuService, restaurantDao));
        server.createContext("/menu-items", ex -> handleMenuItems(ex, menuService));
        server.createContext("/",ex ->{sendText(ex,200,"AAAAAAAAA");});
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
                    sendText(ex, 200, "Price updated");
                    return;
                }

                if (parts.length == 3 && "DELETE".equalsIgnoreCase(ex.getRequestMethod())) {
                    menuService.deleteItem(itemId);
                    sendText(ex, 200, "Deleted");
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
}
