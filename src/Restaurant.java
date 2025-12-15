public class Restaurant {

    private String name;
    private MenuItem[] menu;

    public Restaurant() {
    }

    public Restaurant(String name, MenuItem[] menu) {
        this.name = name;
        this.menu = menu;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMenu(MenuItem[] menu) {
        this.menu = menu;
    }

    public MenuItem findMostExpensive() {
        MenuItem max = menu[0];
        for (MenuItem item : menu) {
            if (item.getPrice() > max.getPrice()) {
                max = item;
            }
        }
        return max;
    }

    public void printMenu() {
        System.out.println("=== MENU of " + name + " ===");
        for (MenuItem item : menu) {
            item.printInfo();
        }
    }
}


