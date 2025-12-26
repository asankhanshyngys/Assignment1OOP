public class DrinkItem extends MenuItem {
    public DrinkItem(String name, double price) {
        super(name, price, "Drink");
    }
    @Override
    public void printInfo(){
        System.out.println("Drink: " + super.toString());
    }
}
