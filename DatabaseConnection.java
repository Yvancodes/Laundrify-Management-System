package Laundry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Define your credentials here ONCE for the entire system
    private static final String DB_URL = "jdbc:mysql://localhost:3306/laundrify_db";
    private static final String DB_USER = "root"; 
    private static final String DB_PASS = ""; // 

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
}