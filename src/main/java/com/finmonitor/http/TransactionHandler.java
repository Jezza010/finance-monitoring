package com.finmonitor.http;

import com.finmonitor.model.jdbc.Transaction;
import com.finmonitor.repository.TransactionRepository;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TransactionHandler {
    private final TransactionRepository repo = new TransactionRepository();
    private static final Gson gson = new Gson();

    private void handleReq(HttpExchange exchange, Function<Map<String, String>, Object> handler) {
        try {
            resp(exchange, handler.apply(getQuery(exchange)), 200);
        } catch (Exception e) {
            e.printStackTrace();
            resp(exchange, Map.of("error", e.getMessage()), 500);
        }
    }

    private static void resp(HttpExchange exchange, Object resp, int status) {
        byte[] bytes = gson.toJson(resp).getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        try {
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> getQuery(HttpExchange exchange) {
        String query = exchange.getRequestURI().getRawQuery();
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    public void transaction(HttpExchange exchange) {
        handleReq(exchange,
                __ -> switch (exchange.getRequestMethod()) {
                    case "GET" -> transactionGet();
                    case "POST" -> transactionPost(exchange);
                    default -> throw new RuntimeException("Invalid method: " + exchange.getRequestMethod());
                });
    }

    private List<Transaction> transactionGet() {
        return repo.findAll();
    }

    private Transaction transactionPost(HttpExchange exchange) {
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            Transaction transaction = gson.fromJson(reader, Transaction.class);
            repo.save(transaction);
            return transaction;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void transactionsCount(HttpExchange exchange) {
        handleReq(exchange, query -> repo.getTransactionCount(query.get("period")));
    }

    public void debetCount(HttpExchange exchange) {
        handleReq(exchange, query -> repo.getDebetCount(query.get("period")));
    }

    public void creditCount(HttpExchange exchange) {
        handleReq(exchange, query -> repo.getCreditCount(query.get("period")));
    }

    public void sumIncome(HttpExchange exchange) {
        handleReq(exchange, query -> repo.getSumIncome(query.get("period")));
    }

    public void sumOutcome(HttpExchange exchange) {
        handleReq(exchange, query -> repo.getSumOutcome(query.get("period")));
    }

    public void completedTransactions(HttpExchange exchange) {
        handleReq(exchange, query -> repo.getCompletedTransactions(query.get("period")));
    }

    public void cancelledTransactions(HttpExchange exchange) {
        handleReq(exchange, query -> repo.getCancelledTransactions(query.get("period")));
    }

    public void bankIncomeStats(HttpExchange exchange) {
        handleReq(exchange, query -> repo.getBankIncomeStats(query.get("period")));
    }

    public void bankOutcomeStats(HttpExchange exchange) {
        handleReq(exchange, query -> repo.getBankOutcomeStats(query.get("period")));
    }
    public void categoryStats(HttpExchange exchange) {
        handleReq(exchange, query -> repo.getCategoryStats(query.get("period")));
    }
}
