package com.finmonitor;

import com.finmonitor.handler.TransactionHandler;
import com.finmonitor.util.DatabaseConnector;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws IOException {
        try (Connection conn = DatabaseConnector.connect()) {
            System.out.println("Успешное соединение");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/transactions", new TransactionHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Сервер запущен. Порт 8080");
    }
}