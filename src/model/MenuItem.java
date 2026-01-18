package model;
import java.util.Formatter;
import java.util.Objects;

public class MenuItem {
    private int id;
    private int restaurantId;
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

    public MenuItem(int id, int restaurantId, String name, double price ,String category){
        this.id = id;
        this.restaurantId = restaurantId;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public int getId() {return id; }
    public int getRestaurantId() {return restaurantId; }
    public String getName() {
        return name;
    }
    public double getPrice() {
        return price;
    }
    public String getCategory() {
        return category;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    public void setID(int id){
        this.id = id;
    }
    public void setRestaurantId(int restaurantId){
        this.restaurantId = restaurantId;
    }

    public void printInfo() {
        System.out.println(this);
    }

    @Override
    public String toString(){
        return name + "[" + category + "] - " + price + "₸";
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof MenuItem))
            return false;
        MenuItem other = (MenuItem) o;
        return name != null
                &&name.equalsIgnoreCase(other.name)
                && Double.compare(price, other.price) == 0
                && Objects.equals(category, other.category);
    }

    @Override
    public int hashCode(){
        return Objects.hash(name == null ? null : name.toLowerCase(),price, category);
    }
}