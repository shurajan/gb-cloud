package com.geekbrains.cloud.server.services;

import java.sql.*;

public class AuthService {
    private static Connection connection;
    private static Statement stmt;


    public void start() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:db/users.db");
            stmt = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized String  getIdByLoginAndPassword(String login, String password) {
        try (ResultSet rs = stmt.executeQuery(
                "SELECT id " +
                        "FROM users " +
                        "WHERE username = '" + login + "' " +
                        "AND password = '" + password + "';")
        ) {

            while (rs.next()) {
                return rs.getString("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void end() {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
