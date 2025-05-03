package com.finmonitor.http;

import com.finmonitor.model.jdbc.Transaction;
import com.finmonitor.repository.TransactionRepository;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class TransactionHandler {
    private final TransactionRepository repo = new TransactionRepository();
    private static final Gson gson = new Gson();
    private static final Set<String> NON_EDITABLE_STATUSES = Set.of(
            "подтвержденная", "в обработке", "отменена", "платеж выполнен", "платеж удален", "возврат"
    );

    public void handleReq(HttpExchange exchange, Function<Map<String, String>, Object> handler) {
        try {
            Object result = handler.apply(getQuery(exchange));
            resp(exchange, result, 200);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            resp(exchange, Map.of("error", "Invalid request data: " + e.getMessage()), 400);
        } catch (Exception e) {
            e.printStackTrace();
            resp(exchange, Map.of("error", "Internal server error: " + e.getMessage()), 500);
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
        handleReq(exchange, query -> {
            switch (exchange.getRequestMethod()) {
                case "GET" -> {
                    return repo.findByFilters(query);
                }
                case "POST" -> {
                    return transactionPost(exchange);
                }
                case "PUT" -> {
                    updateTransaction(exchange);
                    return null;
                }
                case "DELETE" -> {
                    deleteTransaction(exchange);
                    return null;
                }
                default -> throw new RuntimeException("Invalid method: " + exchange.getRequestMethod());
            }
        });
    }

    private Transaction transactionPost(HttpExchange exchange) {
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            Transaction transaction = gson.fromJson(reader, Transaction.class);
            long id = repo.save(transaction);
            transaction.setId(id);
            return transaction;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void updateTransaction(HttpExchange exchange) {
        handleReq(exchange, query -> {
            try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
                Transaction tx = gson.fromJson(reader, Transaction.class);
                Transaction existing = repo.findById(tx.getId());

                if (existing == null) throw new RuntimeException("Transaction not found");
                if (NON_EDITABLE_STATUSES.contains(existing.getStatus().toLowerCase()))
                    throw new RuntimeException("Transaction status does not allow editing");

                repo.update(tx);

                return tx;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void deleteTransaction(HttpExchange exchange) {
        handleReq(exchange, query -> {
            long id = Long.parseLong(query.get("id"));
            Transaction tx = repo.findById(id);

            if (tx == null) throw new RuntimeException("Transaction not found");
            if (NON_EDITABLE_STATUSES.contains(tx.getStatus().toLowerCase()))
                throw new RuntimeException("Cannot delete transaction with current status");

            repo.markAsDeleted(id);
            return Map.of("deleted", true);
        });
    }

    public void updateCategory(HttpExchange exchange) {
        handleReq(exchange, query -> {
            long id = Long.parseLong(query.get("id"));
            String newCategory = query.get("category");
            repo.updateCategory(id, newCategory);
            return Map.of("updated", true);
        });
    }

    public void exportReport(HttpExchange exchange) {
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
            resp(exchange, Map.of("error", "Internal server error: " + e.getMessage()), 500);
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
