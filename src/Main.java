import java.util.Arrays;
import java.util.Scanner;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        MenuItem burger = new FoodItem("Burger", 2500);
        MenuItem cola = new DrinkItem("Cola", 600);
        MenuItem fries = new FoodItem("Fries", 800);
        MenuItem cake = new FoodItem("Chocolate Cake", 1500);
        MenuItem stake = new FoodItem("Stake", 4000);
        MenuItem coffee = new DrinkItem("Coffee", 400);

        MenuItem[] menu = { burger, cola, fries, cake ,stake,coffee};
        Restaurant restaurant = new Restaurant("Shyngys Restaurant", menu);

        restaurant.printMenu();

        Scanner s = new Scanner(System.in);
        ArrayList<MenuItem> cart = new ArrayList<>();

        System.out.println("\nType the item name to add to your order.");
        System.out.println("Type '0' to finish.\n");

        while(true){
            System.out.print("Enter item name: ");
            String input = s.nextLine();
            if(input.equalsIgnoreCase("0")){
                break;
            }
            MenuItem found = restaurant.searchByName(input);
            if (found != null){
                cart.add(found);
                System.out.println(found.getName() + " added to your order!");
            }
            else{
                System.out.println("Item not found in menu. Try again.");
            }
        }

        if(cart.isEmpty()){
            System.out.println("\nNo item selected.Order canceledl.");
        }
        else{
            MenuItem[] orderArray = cart.toArray(new MenuItem[0]);
            Order ord = new Order(1,orderArray);
            System.out.println("\n === YOUR ORDER ===");
            ord.printOrder();
        }
        s.close();
    }
}
