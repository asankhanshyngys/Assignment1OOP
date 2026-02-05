package dao;
import db.Db;
import model.Restaurant;
import repository.RestaurantRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestaurantDao implements RestaurantRepository {
    public int createRestaurant(String name) throws  SQLException{
        String sql = "INSERT INTO restaurants(name) VALUES (?) RETURNING id";
        try(Connection con = Db.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()){
                rs.next();
                return rs.getInt("id");
            }
        }
    }

    public List<Restaurant> findAll() throws SQLException{
        String sql = "SELECT id,name FROM restaurants ORDER BY id";
        List<Restaurant> list = new ArrayList<>();
        try(Connection con = Db.getConnection();
            PreparedStatement ps =con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){
            while(rs.next()){
                list.add(new Restaurant(rs.getInt("id"),rs.getString("name"),new ArrayList<>()));
            }
        }
        return list;
    }

    public Restaurant findById(int id) throws SQLException{
        String sql = "SELECT id, name FROM restaurants WHERE id = ?";
        try(Connection con = Db.getConnection();
            PreparedStatement ps =con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new Restaurant(rs.getInt(id), rs.getString("name"), new ArrayList<>());
            }
        }
    }

    public void updateName(int id, String newName) throws SQLException {
        String sql = "UPDATE restaurants SET name=? WHERE id=?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public Integer findRestaurantIdByName(String name) throws SQLException{
        String sql = "SELECT id FROM restaurants WHERE name = ?";
        try(Connection con = Db.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1,name);
            try(ResultSet rs = ps.executeQuery()){
                if(!rs.next()){
                    return null;
                }
                return rs.getInt("id");
            }
        }
    }
    public void deleteRestaurant(int id) throws SQLException{
        String sql = "DELETE FROM restaurants WHERE id=?";
        try(Connection con = Db.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)){
                ps.setInt(1,id);
                ps.executeUpdate();
        }
    }
}
