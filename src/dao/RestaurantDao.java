package dao;
import db.Db;
import java.sql.*;

public class RestaurantDao {
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
