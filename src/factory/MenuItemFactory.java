package factory;
import model.*;
import exception.InvalidInput;

public class MenuItemFactory {
    public static MenuItem create(String category, String name, double price) {
        if (category == null)
            throw new InvalidInput("Category required");
        if (category.equalsIgnoreCase("Drink"))
            return new DrinkItem(name, price);
        if(category.equalsIgnoreCase("Food"))
            return new FoodItem(name,price);
        throw new InvalidInput("Category must be Food or Drink");
    }
}

