package repository;
import java.sql.SQLException;

public interface RestaurantRepository {
    int createRestaurant(String name) throws SQLException;
    Integer findRestaurantIdByName(String name) throws SQLException;
    void deleteRestaurant(int id) throws SQLException;
}
