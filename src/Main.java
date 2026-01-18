import dao.MenuItemDao;
import dao.RestaurantDao;
import model.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        RestaurantDao restaurantDao = new RestaurantDao();
        MenuItemDao menuItemDao = new MenuItemDao();

        try {
            String restaurantName = "Shyngys Restaurant";
            Integer restaurantId = restaurantDao.findRestaurantIdByName(restaurantName);

            if (restaurantId == null) {
                restaurantId = restaurantDao.createRestaurant(restaurantName);
                System.out.println("Restaurant created in DB.");
            }
            List<MenuItem> menuFromDb = menuItemDao.findMenuByRestaurant(restaurantId);

            if (menuFromDb.isEmpty()) {
                System.out.println("Menu empty. Inserting default items...");

                menuItemDao.createMenuItem(new FoodItem("Burger", 2500));
                menuItemDao.createMenuItem(new DrinkItem("Cola", 600));
                menuItemDao.createMenuItem(new FoodItem("Fries", 800));
                menuItemDao.createMenuItem(new FoodItem("Chocolate Cake", 1500));
                menuItemDao.createMenuItem(new FoodItem("Stake", 4000));
                menuItemDao.createMenuItem(new DrinkItem("Coffee", 400));

                menuFromDb = menuItemDao.findMenuByRestaurant(restaurantId);
            }
            for (MenuItem item : menuFromDb) {
                item.setRestaurantId(restaurantId);
            }

            Restaurant restaurant = new Restaurant(restaurantId, restaurantName, menuFromDb);
            while (true) {
                System.out.println("\n=== MAIN MENU ===");
                System.out.println("1. Show menu");
                System.out.println("2. Add menu item");
                System.out.println("3. Update item price");
                System.out.println("4. Delete item");
                System.out.println("5. Make order");
                System.out.println("0. Exit");

                System.out.print("Choose option: ");
                String choice = sc.nextLine();

                switch (choice) {

                    case "1":
                        restaurant.printMenu();
                        break;

                    case "2": {
                        System.out.print("Item name: ");
                        String name = sc.nextLine();
                        System.out.print("Price: ");
                        double price = Double.parseDouble(sc.nextLine());
                        System.out.print("Category (Food/Drink): ");
                        String category = sc.nextLine();

                        MenuItem item = category.equalsIgnoreCase("Drink")
                                ? new DrinkItem(name, price)
                                : new FoodItem(name, price);

                        item.setRestaurantId(restaurantId);
                        int id = menuItemDao.createMenuItem(item);
                        item.setID(id);
                        restaurant.getMenu().add(item);

                        System.out.println("Item added.");
                        break;
                    }

                    case "3": {
                        restaurant.printMenu();
                        System.out.print("Item index: ");
                        int idx = Integer.parseInt(sc.nextLine());

                        MenuItem item = restaurant.searchByName(idx);
                        if (item != null) {
                            System.out.print("New price: ");
                            double newPrice = Double.parseDouble(sc.nextLine());
                            menuItemDao.updatePrice(item.getId(), newPrice);
                            item.setPrice(newPrice);
                            System.out.println("Price updated.");
                        }
                        break;
                    }

                    case "4": {
                        restaurant.printMenu();
                        System.out.print("Item index to delete: ");
                        int idx = Integer.parseInt(sc.nextLine());

                        MenuItem item = restaurant.searchByName(idx);
                        if (item != null) {
                            menuItemDao.deleteItem(item.getId());
                            restaurant.getMenu().remove(item);
                            System.out.println("Item deleted.");
                        }
                        break;
                    }

                    case "5": {
                        ArrayList<MenuItem> cart = new ArrayList<>();

                        restaurant.printMenu();
                        System.out.println("Enter item name or index (0 to finish):");

                        while (true) {
                            String input = sc.nextLine();
                            if (input.equals("0")) break;

                            MenuItem found = input.matches("\\d+")
                                    ? restaurant.searchByName(Integer.parseInt(input))
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

                    case "0":
                        System.out.println("Goodbye!");
                        sc.close();
                        return;
                }
            }

        } catch (SQLException e) {
            System.out.println("Database error:");
            e.printStackTrace();
        }
    }
}