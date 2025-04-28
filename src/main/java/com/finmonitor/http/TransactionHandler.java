package com.finmonitor.handler;

import com.sun.net.httpserver.*;
import com.finmonitor.model.Transaction;
import com.finmonitor.repository.TransactionRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.google.gson.*;

public class TransactionHandler implements HttpHandler {
    private final TransactionRepository repo = new TransactionRepository();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        switch (method) {
            case "GET" -> handleGet(exchange);
            case "POST" -> handlePost(exchange);
            default -> exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        List<Transaction> transactions = repo.findAll();
        String json = gson.toJson(transactions);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, json.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(json.getBytes());
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        Transaction transaction = gson.fromJson(reader, Transaction.class);
        repo.save(transaction);

        String response = gson.toJson(transaction);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(201, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
