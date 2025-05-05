package com.finmonitor.http;

import com.finmonitor.model.Session;
import com.finmonitor.model.jdbc.Transaction;
import com.finmonitor.repository.SessionRepository;
import com.finmonitor.repository.TransactionRepository;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class TransactionHandler extends BaseHandler {
    private final TransactionRepository repo;
    private static final Gson gson = new Gson();

    public TransactionHandler(SessionRepository sessionRepo, TransactionRepository transactionRepo) {
        super(sessionRepo);
        this.repo = transactionRepo;
    }

    public void handleReq(HttpExchange exchange, Function<Map<String, String>, Object> handler) {
        Optional<Session> session = validateSession(exchange);
        if (session.isEmpty()) return;

        try {
            Object result = handler.apply(getQuery(exchange));
            sendResponse(exchange, 200, result);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            sendResponse(exchange, 400, Map.of("error", "Invalid request data: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, Map.of("error", "Internal server error: " + e.getMessage()));
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
        Optional<Session> session = validateSession(exchange);
        if (session.isEmpty()) return;

        handleReq(exchange, query -> switch (exchange.getRequestMethod()) {
            case "GET" -> repo.findByFilters(query, session.get().getUserId());
            case "POST" -> transactionPost(exchange, session.get().getUserId());
            default -> throw new RuntimeException("Invalid method: " + exchange.getRequestMethod());
        });
    }

    private Transaction transactionPost(HttpExchange exchange, int userId) {
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            Transaction transaction = gson.fromJson(reader, Transaction.class);
            long id = repo.save(transaction, userId);
            transaction.setId(id);
            return transaction;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateTransaction(HttpExchange exchange) {
        Optional<Session> session = validateSession(exchange);
        if (session.isEmpty()) return;

        handleReq(exchange, query -> {
            try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
                Transaction tx = gson.fromJson(reader, Transaction.class);
                return repo.update(tx, session.get().getUserId());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void exportReport(HttpExchange exchange) {
        Optional<Session> session = validateSession(exchange);
        if (session.isEmpty()) return;

        try {
            byte[] csvBytes = repo.getReport().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/csv");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=report.csv");
            try {
                exchange.sendResponseHeaders(200, csvBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(csvBytes);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    public void deleteTransaction(HttpExchange exchange) {
        Optional<Session> session = validateSession(exchange);
        if (session.isEmpty()) return;

        handleReq(exchange, query -> repo.markAsDeleted(Long.parseLong(query.get("id")), session.get().getUserId()));
    }

    public void updateCategory(HttpExchange exchange) {
        Optional<Session> session = validateSession(exchange);
        if (session.isEmpty()) return;

        handleReq(exchange, query -> repo.updateCategory(Long.parseLong(query.get("id")), query.get("category"), session.get().getUserId()));
    }

    public void createCategory(HttpExchange exchange) {
        Optional<Session> session = validateSession(exchange);
        if (session.isEmpty()) return;

        handleReq(exchange, query -> repo.createCategory(query.get("category"), session.get().getUserId()));
    }

    public void transactionsCount(HttpExchange exchange) {
        Optional<Session> session = validateSession(exchange);
        if (session.isEmpty()) return;

        handleReq(exchange, query -> repo.getTransactionCount(query.get("period"), session.get().getUserId()));
    }

    public void debetCount(HttpExchange exchange) {
        Optional<Session> session = validateSession(exchange);
        if (session.isEmpty()) return;

        handleReq(exchange, query -> repo.getDebetCount(query.get("period"), session.get().getUserId()));
    }

    public void creditCount(HttpExchange exchange) {
        Optional<Session> session = validateSession(exchange);
        if (session.isEmpty()) return;

        handleReq(exchange, query -> repo.getCreditCount(query.get("period"), session.get().getUserId()));
    }

    public void sumIncome(HttpExchange exchange) {
        Optional<Session> session = validateSession(exchange);
        if (session.isEmpty()) return;

        handleReq(exchange, query -> repo.getSumIncome(query.get("period"), session.get().getUserId()));
    }

    public void sumOutcome(HttpExchange exchange) {
        Optional<Session> session = validateSession(exchange);
        if (session.isEmpty()) return;

        handleReq(exchange, query -> repo.getSumOutcome(query.get("period"), session.get().getUserId()));
    }

    public void completedTransactions(HttpExchange exchange) {
        Optional<Session> session = validateSession(exchange);
        if (session.isEmpty()) return;

        handleReq(exchange, query -> repo.getCompletedTransactions(query.get("period"), session.get().getUserId()));
    }

    public void cancelledTransactions(HttpExchange exchange) {
        Optional<Session> session = validateSession(exchange);
        if (session.isEmpty()) return;

        handleReq(exchange, query -> repo.getCancelledTransactions(query.get("period"), session.get().getUserId()));
    }

    public void bankIncomeStats(HttpExchange exchange) {
        Optional<Session> session = validateSession(exchange);
        if (session.isEmpty()) return;

        handleReq(exchange, query -> repo.getBankIncomeStats(query.get("period"), session.get().getUserId()));
    }

    public void bankOutcomeStats(HttpExchange exchange) {
        Optional<Session> session = validateSession(exchange);
        if (session.isEmpty()) return;

        handleReq(exchange, query -> repo.getBankOutcomeStats(query.get("period"), session.get().getUserId()));
    }

    public void categoryStats(HttpExchange exchange) {
        Optional<Session> session = validateSession(exchange);
        if (session.isEmpty()) return;

        handleReq(exchange, query -> repo.getCategoryStats(query.get("period"), session.get().getUserId()));
    }

    @Override
    public void handle(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();

        switch (path) {
            case "/api/transaction" -> transaction(exchange);
            case "/api/transactions_count" -> transactionsCount(exchange);
            case "/api/debet_count" -> debetCount(exchange);
            case "/api/credit_count" -> creditCount(exchange);
            case "/api/sum_income" -> sumIncome(exchange);
            case "/api/sum_outcome" -> sumOutcome(exchange);
            case "/api/completed_transactions" -> completedTransactions(exchange);
            case "/api/cancelled_transactions" -> cancelledTransactions(exchange);
            case "/api/bank_income_stats" -> bankIncomeStats(exchange);
            case "/api/bank_outcome_stats" -> bankOutcomeStats(exchange);
            case "/api/category_stats" -> categoryStats(exchange);
            case "/api/update_transaction" -> updateTransaction(exchange);
            case "/api/delete_transaction" -> deleteTransaction(exchange);
            case "/api/create_category" -> createCategory(exchange);
            case "/api/update_category" -> updateCategory(exchange);
            case "/api/export" -> exportReport(exchange);
            default -> sendResponse(exchange, 404, Map.of("error", "Not found"));
        }
    }
}
