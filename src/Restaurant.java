import java.util.Arrays;
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
    public void filterByCategory(String category){
        System.out.println("\nFiltered by: " + category);
        for(MenuItem item : menu){
            if(item.getCategory().equalsIgnoreCase(category))
                item.printInfo();
        }
    }
    public void sortByPrice(){
        Arrays.sort(menu, (a,b) -> Double.compare(a.getPrice(),b.getPrice()));
        System.out.println("\nMenu sorted by price (cheapest first:)");
        printMenu();
    }

    public MenuItem searchByName(String name){
        for(MenuItem item : menu){
            if (item.getName().equalsIgnoreCase(name)){
                return item;
            }
        }
        return null;
    }

    public MenuItem searchByName(int index){
        if(index >= 0 && index < menu.length){
            return menu[index];
        }
        return null;
    }
}


