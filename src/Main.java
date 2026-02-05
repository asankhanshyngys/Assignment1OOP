import dao.MenuItemDao;
import dao.RestaurantDao;
import model.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import API.RestServer;
import java.io.IOException;
import Service.MenuService;
import exception.InvalidInput;
import model.Restaurant;
import repository.MenuItemRepository;

public class Main {
    public static void main(String[] args) {
        try {
            RestServer.start();
        } catch (IOException e) {
            System.out.println("API error: " + e.getMessage());
        }

        Scanner sc = new Scanner(System.in);

        RestaurantDao restaurantDao = new RestaurantDao();
        MenuItemRepository repo = new MenuItemDao();
        MenuService menuService = new MenuService(repo);

        try {
            String restaurantName = "Shyngys Restaurant";
            Integer restaurantId = restaurantDao.findRestaurantIdByName(restaurantName);

            if (restaurantId == null) {
                restaurantId = restaurantDao.createRestaurant(restaurantName);
                System.out.println("Restaurant created in DB.");
            }
            List<MenuItem> menuFromDb = repo.findMenuByRestaurant(restaurantId);

            Restaurant restaurant = new Restaurant(restaurantId, restaurantName, menuFromDb);
            Restaurant currentRestaurant = null;

            while (true) {
                System.out.println("\n=== MAIN MENU ===");
                System.out.println("Selected: " + (currentRestaurant == null ? "NONE" : currentRestaurant.getName()));
                System.out.println("1. List restaurants");
                System.out.println("2. Create restaurant");
                System.out.println("3. Select restaurant");
                System.out.println("4. Update restaurant name");
                System.out.println("5. Delete restaurant");
                System.out.println("6. Manage menu of selected restaurant");
                System.out.println("0. Exit");

                System.out.print("Choose option: ");
                String resChoice = sc.nextLine();

                switch (resChoice) {

                    case "1": {
                        List<Restaurant> list = restaurantDao.findAll();
                        printRestaurants(list);
                        break;
                    }

                    case "2": {
                        System.out.print("New restaurant name: ");
                        String name = sc.nextLine();

                        Integer id = restaurantDao.findRestaurantIdByName(name);
                        if (id != null) {
                            System.out.println("Restaurant already exists.");
                            break;
                        }

                        int newId = restaurantDao.createRestaurant(name);
                        System.out.println("Restaurant created (id=" + newId + ").");
                        break;
                    }

                    case "3": {
                        List<Restaurant> list = restaurantDao.findAll();
                        if (list.isEmpty()) {
                            System.out.println("No restaurants. Create one first.");
                            break;
                        }

                        printRestaurants(list);
                        System.out.print("Choose restaurant number: ");
                        int num = Integer.parseInt(sc.nextLine());

                        if (num < 1 || num > list.size()) {
                            System.out.println("Invalid choice.");
                            break;
                        }

                        Restaurant r = list.get(num - 1);
                        List<MenuItem> menu = repo.findMenuByRestaurant(r.getId());
                        currentRestaurant = new Restaurant(r.getId(), r.getName(), menu);

                        System.out.println("Selected: " + currentRestaurant.getName());
                        break;
                    }

                    case "4": {
                        List<Restaurant> list = restaurantDao.findAll();
                        if (list.isEmpty()) {
                            System.out.println("No restaurants.");
                            break;
                        }

                        printRestaurants(list);
                        System.out.print("Choose restaurant number to rename: ");
                        int num = Integer.parseInt(sc.nextLine());

                        if (num < 1 || num > list.size()) {
                            System.out.println("Invalid choice.");
                            break;
                        }

                        Restaurant r = list.get(num - 1);

                        System.out.print("New name: ");
                        String newName = sc.nextLine();

                        restaurantDao.updateName(r.getId(), newName);
                        System.out.println("Restaurant renamed.");

                        if (currentRestaurant != null && currentRestaurant.getId() == r.getId()) {
                            currentRestaurant = new Restaurant(r.getId(), newName, currentRestaurant.getMenu());
                        }
                        break;
                    }

                    case "5": {
                        List<Restaurant> list = restaurantDao.findAll();
                        if (list.isEmpty()) {
                            System.out.println("No restaurants.");
                            break;
                        }

                        printRestaurants(list);
                        System.out.print("Choose restaurant number to delete: ");
                        int num = Integer.parseInt(sc.nextLine());

                        if (num < 1 || num > list.size()) {
                            System.out.println("Invalid choice.");
                            break;
                        }

                        Restaurant r = list.get(num - 1);
                        restaurantDao.deleteRestaurant(r.getId());
                        System.out.println("Restaurant deleted.");

                        if (currentRestaurant != null && currentRestaurant.getId() == r.getId()) {
                            currentRestaurant = null;
                        }
                        break;
                    }

                    case "6": {
                        if (currentRestaurant == null) {
                            System.out.println("Select a restaurant first (option 3).");
                            break;
                        }
                        manageMenu(sc, currentRestaurant, repo, menuService);
                        List<MenuItem> menu = repo.findMenuByRestaurant(currentRestaurant.getId());
                        currentRestaurant = new Restaurant(currentRestaurant.getId(), currentRestaurant.getName(), menu);
                        break;
                    }

                    case "0":
                        System.out.println("Goodbye!");
                        return;

                    default:
                        System.out.println("Unknown option.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Database connection failed.");
            System.out.println("Reason: " + e.getMessage());
        }
    }

    private static void printRestaurants(List<Restaurant> list) {
        System.out.println("=== RESTAURANTS ===");
        for (int i = 0; i < list.size(); i++) {
            Restaurant r = list.get(i);
            System.out.println(
                    (i + 1) + "." + r.getName()
            );
        }
    }
    private static void manageMenu(Scanner sc,
                                   Restaurant restaurant,
                                   MenuItemRepository repo,
                                   MenuService menuService) throws SQLException {

        while (true) {
            System.out.println("\n=== MENU MANAGEMENT: " + restaurant.getName() + " ===");
            System.out.println("1. Show menu");
            System.out.println("2. Add menu item");
            System.out.println("3. Update item");
            System.out.println("4. Delete item");
            System.out.println("5. Make order");
            System.out.println("6. Manage order");
            System.out.println("0. Back");

            System.out.print("Choose option: ");
            String menuChoice = sc.nextLine().trim();

            switch (menuChoice) {

                case "1": {
                    restaurant.getMenu().clear();
                    restaurant.getMenu().addAll(repo.findMenuByRestaurant(restaurant.getId()));
                    restaurant.printMenu();
                    break;
                }

                case "2": {
                    try {
                        System.out.print("Item name: ");
                        String name = sc.nextLine();

                        System.out.print("Price: ");
                        double price = Double.parseDouble(sc.nextLine());

                        System.out.print("Category (Food/Drink): ");
                        String category = sc.nextLine();

                        MenuItem item = category.equalsIgnoreCase("Drink")
                                ? new DrinkItem(name, price)
                                : new FoodItem(name, price);

                        item.setRestaurantId(restaurant.getId());

                        int id = menuService.addItem(item);
                        item.setID(id);

                        restaurant.getMenu().add(item);
                        System.out.println("Item added.");
                    } catch (InvalidInput e) {
                        System.out.println("Input error: " + e.getMessage());
                    } catch (NumberFormatException e) {
                        System.out.println("Input error: price must be a number.");
                    }
                    break;
                }

                case "3": {
                    restaurant.printMenu();
                    System.out.print("Item number to update: ");
                    int num = Integer.parseInt(sc.nextLine());

                    MenuItem item = restaurant.getByNumber(num);
                    if (item == null) {
                        System.out.println("Item not found.");
                        break;
                    }

                    System.out.print("New name (Enter to keep '" + item.getName() + "'): ");
                    String newName = sc.nextLine();
                    if (newName.isBlank()) newName = item.getName();

                    System.out.print("New price (Enter to keep " + item.getPrice() + "): ");
                    String priceStr = sc.nextLine();
                    double newPrice = priceStr.isBlank() ? item.getPrice() : Double.parseDouble(priceStr);

                    System.out.print("New category Food/Drink (Enter to keep " + item.getCategory() + "): ");
                    String newCat = sc.nextLine();
                    if (newCat.isBlank()) newCat = item.getCategory();

                    try {
                        menuService.updateItem(item.getId(), newName, newPrice, newCat);

                        item.setName(newName);
                        item.setPrice(newPrice);
                        item.setCategory(newCat);

                        System.out.println("Item updated.");
                    } catch (InvalidInput e) {
                        System.out.println("Input error: " + e.getMessage());
                    }
                    break;
                }

                case "4": {
                    restaurant.printMenu();
                    System.out.print("Item number to delete (1..n): ");
                    int num = Integer.parseInt(sc.nextLine());

                    MenuItem item = restaurant.getByNumber(num);
                    if (item == null) {
                        System.out.println("Item not found.");
                        break;
                    }

                    menuService.deleteItem(item.getId());
                    restaurant.getMenu().remove(item);
                    System.out.println("Item deleted.");
                    break;
                }

                case "5": {
                    restaurant.printMenu();
                    System.out.println("Enter item number (0 to finish):");

                    ArrayList<MenuItem> cart = new ArrayList<>();
                    while (true) {
                        String input = sc.nextLine().trim();
                        if (input.equals("0")) break;

                        MenuItem found = input.matches("\\d+")
                                ? restaurant.getByNumber(Integer.parseInt(input))
                                : restaurant.searchByName(input);

                        if (found != null) {
                            cart.add(found);
                            System.out.println(found.getName() + " added.");
                        } else {
                            System.out.println("Item not found.");
                        }
                    }

                    if (!cart.isEmpty()) {
                        Order order = new Order(1, cart.toArray(new MenuItem[0]));
                        order.printOrder();
                    }
                    break;
                }
                case "6":{
                    restaurant.sortByPrice();
                    restaurant.printMenu();
                    System.out.println("Menu sorted by price.");
                    break;
                }

                case "0":
                    return;

                default:
                    System.out.println("Unknown option.");
            }
        }
    }

}
