public class MenuItem {

    private String name;
    private double price;
    private String category;

    public MenuItem() {
    }

    public MenuItem(String name, double price, String category) {
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }


    public void printInfo() {
        System.out.println(name + " (" + category + ") - " + price + "₸");
    }
}