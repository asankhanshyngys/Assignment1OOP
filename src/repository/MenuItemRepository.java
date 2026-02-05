package repository;
import model.MenuItem;
import java.sql.SQLException;
import java.util.List;

public interface MenuItemRepository {
    int createMenuItem(MenuItem item) throws SQLException;
    List<MenuItem> findMenuByRestaurant(int restaurantId) throws SQLException;
    void updatePrice(int itemId,double newPrice) throws SQLException;
    void updateItem(int id, String name, double price, String category) throws SQLException;
    void deleteItem(int itemId) throws SQLException;
}
