public class Order {

    private int orderId;
    private MenuItem[] items;

    public Order() {
    }

    public Order(int orderId, MenuItem[] items) {
        this.orderId = orderId;
        this.items = items;
    }


    public double calculateTotal() {
        double total = 0;
        for (MenuItem item : items) {
            total += item.getPrice();
        }
        return total;
    }

    public void printOrder() {
        System.out.println("Order #" + orderId + ":");
        for (MenuItem item : items) {
            item.printInfo();
        }
        System.out.println("Total: " + calculateTotal() + "₸");
    }
}
