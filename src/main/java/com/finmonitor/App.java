package com.finmonitor;

import com.finmonitor.config.JDBCConnector;
import com.finmonitor.http.TransactionHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) throws IOException {
        //try (Connection conn = DatabaseConnector.connect()) {
        try (Connection conn = JDBCConnector.getConnection()) {
            System.out.println("Successful test connection to db!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/v1/transaction", new TransactionHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("The server is running on port 8080...");
    }
}