package model;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Restaurant {
    private int id;
    private String name;
    private List<MenuItem> menu;

    public Restaurant() {
        this.menu = new ArrayList<>();
    }

    public Restaurant(String name, MenuItem[] menu) {
        this.name = name;
        this.menu = new ArrayList<>(Arrays.asList(menu));
    }

    public Restaurant(int id, String name, List<MenuItem> menu){
        this.id = id;
        this.name = name;
        this.menu = menu;
    }

    public int getId(){ return  id; }
    public String getName(){return name; }

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
        menu.sort(Comparator.comparingDouble(MenuItem::getPrice));
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
        if(index >= 0 && index < menu.size()){
            return menu.get(index);
        }
        return null;
    }
    public List<MenuItem> getMenu(){
        return menu;
    }
}


