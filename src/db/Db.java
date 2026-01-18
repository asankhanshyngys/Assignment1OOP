package db;
import  java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db {
    private static final String URL = "jdbc:postgresql://localhost:5432/restaurant-db";
    private static final String USER = "Shyngys";
    private static final String PASS = "Shyngys7681@_";

    public static Connection getConnection()
            throws SQLException{
        return  DriverManager.getConnection(URL, USER, PASS);
    }
}
