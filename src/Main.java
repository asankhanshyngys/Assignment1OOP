class Main {
    public static void main(String[] args) {

        MenuItem burger = new MenuItem("Burger", 2500, "Meal");
        MenuItem cola = new MenuItem("Cola", 600, "Drink");
        MenuItem fries = new MenuItem("Fries", 800, "Meal");
        MenuItem cake = new MenuItem("Chocolate Cake", 1500, "Dessert");
        MenuItem stake = new MenuItem("Stake", 4000, "Meat");

        MenuItem[] menu = { burger, cola, fries, cake };
        Restaurant restaurant = new Restaurant("Shyngys Restaurant", menu);

        restaurant.printMenu();

        MenuItem[] orderItems = { burger, fries, stake };
        Order order1 = new Order(101, orderItems);

        System.out.println("\n=== ORDER DETAILS ===");
        order1.printOrder();

        System.out.println("\n=== MOST EXPENSIVE MENU ITEM ===");
        MenuItem expensive = restaurant.findMostExpensive();
        expensive.printInfo();

        System.out.println("\n=== PRICE CHECK ===");
        if (burger.getPrice() > fries.getPrice()) {
            System.out.println("Burger is more expensive than Fries.");
        } else {
            System.out.println("Fries are more expensive or equal.");
        }

        System.out.println("\n=== LOOP THROUGH ORDER ITEMS ===");
        double maxPrice = 0;

        for (int i = 0; i < orderItems.length; i++) {
            MenuItem item = orderItems[i];
            System.out.println("Item " + (i + 1) + ": " + item.getName() + " - " + item.getPrice() + "₸");

            if (item.getPrice() > maxPrice) {
                maxPrice = item.getPrice();
            }
        }

        System.out.println("Most expensive item in order: " + maxPrice + "₸");
    }
}
