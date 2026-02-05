package builder;
import model.MenuItem;
import model.Restaurant;
import model.Order;
import java.util.ArrayList;
import java.util.List;

public class OrderBuilder {
    private int orderId;
    private final List<MenuItem> items = new ArrayList<>();

    public OrderBuilder id(int id){
        this.orderId = id;
        return this;
    }
    public OrderBuilder add(MenuItem item){
        items.add(item);
        return this;
    }
    public Order build(){
        return new Order(orderId, items.toArray(new MenuItem[0]));
    }
}
