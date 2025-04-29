package com.finmonitor.http;

import com.finmonitor.dao.jdbc.TransactionJDBCDaoImpl;
import com.finmonitor.model.jdbc.Transaction;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class TransactionListHandler implements HttpHandler {
    private final TransactionJDBCDaoImpl dao = new TransactionJDBCDaoImpl();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        System.out.println(method);

        switch (method) {
            case "GET" -> handleGet(exchange);
            default -> exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        List<Transaction> transactions = dao.findAll();

        System.out.println("Find all transactions query, result:\n" + transactions);

        try {
            String json = gson.toJson(transactions);
            System.out.println("json: \n" + json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String json = gson.toJson(transactions);
        System.out.println("json: \n" + json);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, json.getBytes().length);

        System.out.println("Find all transactions query, 2 result:\n" + transactions);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(json.getBytes());
        }
    }
}
