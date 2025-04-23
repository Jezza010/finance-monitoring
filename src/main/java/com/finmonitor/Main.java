package com.finmonitor;

import com.finmonitor.util.DatabaseConnector;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnector.connect()) {
            System.out.println("Успешное соединение");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}