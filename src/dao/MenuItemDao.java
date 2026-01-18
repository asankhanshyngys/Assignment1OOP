package dao;

import db.Db;
import model.MenuItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuItemDao {

    public int createMenuItem(MenuItem item) throws SQLException {
        String sql = "INSERT INTO menu_items(restaurant_id, name, price, category) VALUES (?,?,?,?) RETURNING id";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, item.getRestaurantId());
            ps.setString(2, item.getName());
            ps.setDouble(3, item.getPrice());
            ps.setString(4, item.getCategory());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("id");
            }
        }
    }

    public List<MenuItem> findMenuByRestaurant(int restaurantId) throws SQLException {
        String sql = "SELECT id, restaurant_id, name, price, category FROM menu_items WHERE restaurant_id=? ORDER BY id";
        List<MenuItem> list = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new MenuItem(
                            rs.getInt("id"),
                            rs.getInt("restaurant_id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getString("category")
                    ));
                }
            }
        }
        return list;
    }

    public void updatePrice(int itemId, double newPrice) throws SQLException {
        String sql = "UPDATE menu_items SET price=? WHERE id=?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, newPrice);
            ps.setInt(2, itemId);
            ps.executeUpdate();
        }
    }

    public void deleteItem(int itemId) throws SQLException {
        String sql = "DELETE FROM menu_items WHERE id=?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.executeUpdate();
        }
    }
}

