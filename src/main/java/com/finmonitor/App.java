package com.finmonitor;

import com.finmonitor.config.JDBCConnector;
import com.finmonitor.http.AuthHandler;               // обработчик авторизации
import com.finmonitor.http.TransactionHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) throws IOException {
        try (Connection conn = JDBCConnector.getConnection()) {
            System.out.println("Successful test connection to db!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        //обработчик авторизации
        AuthHandler authHandler = new AuthHandler();
        server.createContext("/api/auth/register", authHandler);
        server.createContext("/api/auth/login", authHandler);

        // обработчик транзакций
        var handler = new TransactionHandler();
        server.createContext("/api/transaction", handler::transaction);
        server.createContext("/api/transactions_count", handler::transactionsCount);
        server.createContext("/api/debet_count", handler::debetCount);
        server.createContext("/api/sum_income", handler::sumIncome);

        server.setExecutor(null);
        server.start();

        System.out.println("The server is running on port 8080...");
    }
}
