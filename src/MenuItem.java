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

    public String getCategory() {
        return category;
    }

    public void setPrice(double price) {
        this.price = price;
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
        return this.equals(other.name) && this.price == other.price;
    }

    @Override
    public int hashCode(){
        return name.hashCode() + (int)price;
    }
}