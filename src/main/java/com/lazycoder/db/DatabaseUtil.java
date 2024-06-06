package com.lazycoder.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    private static final String URL = "jdbc:sqlite:sample.db"; // Update as needed

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
