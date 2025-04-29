package com.finmonitor.http;

import com.finmonitor.dao.jdbc.TransactionJDBCDaoImpl;
import com.sun.net.httpserver.*;
import com.finmonitor.model.jdbc.Transaction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.google.gson.*;

public class TransactionHandler implements HttpHandler {
    private final TransactionJDBCDaoImpl dao = new TransactionJDBCDaoImpl();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        System.out.println(method);

        switch (method) {
            case "POST" -> handlePost(exchange);
            default -> exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        Transaction transaction = gson.fromJson(reader, Transaction.class);
        dao.save(transaction);

        String response = gson.toJson(transaction);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(201, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
