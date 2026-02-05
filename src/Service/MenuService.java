package Service;
import exception.InvalidInput;
import exception.NotFound;
import model.MenuItem;
import repository.MenuItemRepository;
import java.util.List;

import java.sql.SQLException;

public class MenuService {
    private final MenuItemRepository menuRepo;
    public MenuService(MenuItemRepository menuRepo){
        this.menuRepo = menuRepo;
    }
    public List<MenuItem> getMenu(int restaurantId) throws SQLException{
        return menuRepo.findMenuByRestaurant(restaurantId);
    }
    public int addItem(MenuItem item) throws SQLException{
        if(item.getName() == null || item.getName().isBlank())
            throw new InvalidInput("Name cannot be empty");
        if(item.getPrice() < 0)
            throw new InvalidInput("Price cannot be negative");
        return menuRepo.createMenuItem(item);
    }
    public void updatePrice(int itemId, double price) throws SQLException {
        if (price < 0) throw new InvalidInput("Price cannot be negative");
        menuRepo.updatePrice(itemId, price);
    }

    public void updateItem(int id, String name, double price, String category) throws SQLException {
        if (name == null || name.isBlank()) throw new InvalidInput("Name cannot be empty");
        if (price < 0) throw new InvalidInput("Price cannot be negative");
        if (!category.equalsIgnoreCase("Food") && !category.equalsIgnoreCase("Drink"))
            throw new InvalidInput("Category must be Food or Drink");

        menuRepo.updateItem(id, name, price, normalizeCategory(category));
    }

    private String capitalize(String s) {
        String x = s.trim().toLowerCase();
        return x.substring(0,1).toUpperCase() + x.substring(1);
    }


    public void deleteItem(int itemId) throws SQLException{
        menuRepo.deleteItem(itemId);
        }

    private String normalizeCategory(String c) {
        c = c.trim().toLowerCase();

        if (c.equals("food")) return "Food";
        if (c.equals("drink")) return "Drink";

        throw new InvalidInput("Category must be Food or Drink");
    }
}

