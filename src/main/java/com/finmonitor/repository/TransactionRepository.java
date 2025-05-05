package com.finmonitor.repository;

import com.finmonitor.model.jdbc.Transaction;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionRepository {
    private final HikariDataSource dataSource;
    private static final Map<String, Boolean> SUCCESS = Map.of("success", true);
    private static final Set<String> NON_EDITABLE_STATUSES = Set.of(
            "Подтвержденная", "В обработке", "Отменена", "Платеж выполнен", "Платеж удален", "Возврат"
    );

    public TransactionRepository(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long save(Transaction t, int userId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO transactions (user_id, person_type_id, transaction_type_id, status_id, category_id, transaction_datetime, comment, amount, sender_bank, receiver_bank, account_number, receiver_account_number, receiver_inn, receiver_phone) " +
                             "VALUES (?, (SELECT id FROM person_types WHERE name = ?)," +
                             "(SELECT id FROM transaction_types WHERE name = ?)," +
                             "(SELECT id FROM transaction_statuses WHERE name = ?)," +
                             "(SELECT id FROM categories WHERE name = ?)," +
                             "?, ?, ?, ?, ?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, userId);
            stmt.setString(2, t.getPersonType());
            stmt.setString(3, t.getTransactionType());
            stmt.setString(4, t.getStatus());
            if (t.getCategory() != null) {
                stmt.setString(5, t.getCategory());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.parse(t.getDateTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            stmt.setString(7, t.getComment());
            stmt.setDouble(8, t.getAmount());
            stmt.setString(9, t.getSenderBank());
            stmt.setString(10, t.getReceiverBank());
            stmt.setString(11, t.getSenderAccountNumber());
            stmt.setString(12, t.getReceiverAccountNumber());
            stmt.setString(13, t.getReceiverINN());
            stmt.setString(14, t.getPhone());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Creating transaction failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error saving transaction", e);
        }
    }

    public List<Transaction> findByFilters(Map<String, String> filters, int userId) {
        validateDate(filters.get("from_date"));
        validateDate(filters.get("to_date"));
        List<String> params = new ArrayList<>();
        String expr = filters.entrySet().stream().map(e -> {
            params.add(e.getValue());
            return switch (e.getKey()) {
                case "from_bank" -> "sender_bank = ?";
                case "to_bank" -> "receiver_bank = ?";
                case "from_date" -> "transaction_datetime >= to_date(?, 'DD.MM.YYYY')";
                case "to_date" -> "transaction_datetime <= to_date(?, 'DD.MM.YYYY')";
                case "status" -> "status = ?";
                case "inn" -> "receiver_inn = ?";
                case "amount_from" -> "amount >= (?::numeric)";
                case "amount_to" -> "amount <= (?::numeric)";
                case "type" -> "transaction_type = ?";
                case "category" -> "category = ?";
                default -> throw new IllegalArgumentException("Invalid filter: " + e.getKey());
            };
        }).collect(Collectors.joining(" AND "));

        String sql = "SELECT * FROM transactions_full_view WHERE user_id = ?" + (expr.isEmpty() ? "" : " AND " + expr);
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 2, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRow(rs));
                }
                return transactions;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void validateDate(String date) {
        if (date == null) {
            return;
        }

        try {
            LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Дата должна быть в формате 01.01.2025");
        }
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getLong("id"));
        t.setPersonType(rs.getString("person_type"));
        t.setTransactionType(rs.getString("transaction_type"));
        t.setDateTime(rs.getTimestamp("transaction_datetime").toString());
        t.setComment(rs.getString("comment"));
        t.setAmount(rs.getDouble("amount"));
        t.setStatus(rs.getString("status"));
        t.setSenderBank(rs.getString("sender_bank"));
        t.setReceiverBank(rs.getString("receiver_bank"));
        t.setReceiverINN(rs.getString("receiver_inn"));
        t.setReceiverAccountNumber(rs.getString("receiver_account_number"));
        t.setCategory(rs.getString("category"));
        t.setPhone(rs.getString("receiver_phone"));
        return t;
    }

    private String getPeriodAggregate(String period) {
        return switch (period) {
            case "W" -> "to_char(transaction_datetime, 'IYYY-IW')";
            case "M" -> "to_char(transaction_datetime, 'YYYY-MM')";
            case "Q" -> "concat(extract(year FROM transaction_datetime), '-Q', extract(quarter FROM transaction_datetime))";
            case "Y" -> "to_char(transaction_datetime, 'YYYY')";
            default -> throw new IllegalArgumentException("Invalid period: " + period);
        };
    }

    private String getPeriodFilter(String period) {
        return switch (period) {
            case "W" -> "to_char(transaction_datetime, 'IYYY-IW') = to_char(now(), 'IYYY-IW')";
            case "M" -> "to_char(transaction_datetime, 'YYYY-MM') = to_char(now(), 'YYYY-MM')";
            case "Q" -> "to_char(transaction_datetime, 'YYYY-MM') BETWEEN to_char(now() - INTERVAL '3 months', 'YYYY-MM') AND to_char(now(), 'YYYY-MM')";
            case "Y" -> "to_char(transaction_datetime, 'YYYY') = to_char(now(), 'YYYY')";
            default -> throw new IllegalArgumentException("Invalid period: " + period);
        };
    }

    public Transaction findById(long id, int userId) {
        String sql = "SELECT * FROM transactions_full_view WHERE id = ? AND user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding transaction by id", e);
        }
    }

    public Map<String, Boolean> update(Transaction t, int userId) {
        Transaction existing = findById(t.getId(), userId);

        if (existing == null) throw new RuntimeException("Transaction not found");
        if (NON_EDITABLE_STATUSES.contains(existing.getStatus().toLowerCase())) {
            throw new RuntimeException("Transaction status does not allow editing");
        }

        String sql = """
        UPDATE transactions SET
            person_type_id = (SELECT id FROM person_types WHERE name = ?),
            transaction_datetime = ?,
            comment = ?,
            amount = ?,
            status_id = (SELECT id FROM transaction_statuses WHERE name = ?),
            sender_bank = ?,
            receiver_bank = ?,
            receiver_inn = ?,
            category_id = (SELECT id FROM categories WHERE name = ?),
            receiver_phone = ?
        WHERE id = ? AND user_id = ?
    """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, t.getPersonType());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.parse(t.getDateTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            stmt.setString(3, t.getComment());
            stmt.setDouble(4, t.getAmount());
            stmt.setString(5, t.getStatus());
            stmt.setString(6, t.getSenderBank());
            stmt.setString(7, t.getReceiverBank());
            stmt.setString(8, t.getReceiverINN());
            stmt.setString(9, t.getCategory());
            stmt.setString(10, t.getPhone());
            stmt.setLong(11, t.getId());
            stmt.setInt(12, userId);

            stmt.executeUpdate();
            return SUCCESS;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating transaction", e);
        }
    }

    public Map<String, Boolean> markAsDeleted(long id, int userId) {
        Transaction tx = findById(id, userId);

        if (tx == null) {
            throw new RuntimeException("Transaction not found");
        }
        if (NON_EDITABLE_STATUSES.contains(tx.getStatus().toLowerCase())) {
            throw new RuntimeException("Cannot delete transaction with current status");
        }

        String sql = "UPDATE transactions SET status_id = (SELECT id FROM transaction_statuses WHERE name = 'Платеж удален') WHERE id = ? AND user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            return SUCCESS;
        } catch (SQLException e) {
            throw new RuntimeException("Error marking transaction as deleted", e);
        }
    }

    public Map<String, Boolean> updateCategory(long id, String category, int userId) {
        String sql = "UPDATE transactions SET category_id = (SELECT id FROM categories WHERE name = ?) WHERE id = ? AND user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category);
            stmt.setLong(2, id);
            stmt.setInt(3, userId);
            stmt.executeUpdate();
            return SUCCESS;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating category", e);
        }
    }

    public Map<String, Boolean> createCategory(String category, int userId) {
        String sql = "INSERT INTO categories (name, user_id) VALUES (?, ?) ON CONFLICT (name, user_id) DO NOTHING";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            return SUCCESS;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating category", e);
        }
    }

    public String getReport() {
        StringBuilder csv = new StringBuilder();
        String query = "SELECT * FROM transactions_full_view";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                csv.append(meta.getColumnName(i));
                if (i < columnCount) csv.append(",");
            }
            csv.append("\n");

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);
                    if (value != null) {
                        value = value.replace("\"", "\"\"");
                        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                            value = "\"" + value + "\"";
                        }
                    }
                    csv.append(value != null ? value : "");
                    if (i < columnCount) csv.append(",");
                }
                csv.append("\n");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return csv.toString();
    }

    public List<ChartLongItem> getTransactionCount(String period, int userId) {
        String sql = "SELECT " + getPeriodAggregate(period) + " as period, COUNT(*) as count " +
                "FROM transactions WHERE user_id = ? AND " + getPeriodFilter(period) +
                " GROUP BY period ORDER BY period";
        return longSeriesCommon(sql, userId);
    }

    public List<ChartDoubleItem> getDebetCount(String period, int userId) {
        String sql = "SELECT " + getPeriodAggregate(period) + " as period, COUNT(*) as count " +
                "FROM transactions t JOIN transaction_types tt ON t.transaction_type_id = tt.id " +
                "WHERE t.user_id = ? AND tt.name = 'Поступление' AND " + getPeriodFilter(period) +
                " GROUP BY period ORDER BY period";
        return doubleSeriesCommon(sql, userId);
    }

    public List<ChartDoubleItem> getCreditCount(String period, int userId) {
        String sql = "SELECT " + getPeriodAggregate(period) + " as period, COUNT(*) as count " +
                "FROM transactions t JOIN transaction_types tt ON t.transaction_type_id = tt.id " +
                "WHERE t.user_id = ? AND tt.name = 'Списание' AND " + getPeriodFilter(period) +
                " GROUP BY period ORDER BY period";
        return doubleSeriesCommon(sql, userId);
    }

    public List<ChartDoubleItem> getSumIncome(String period, int userId) {
        String sql = "SELECT " + getPeriodAggregate(period) + " as period, SUM(amount) as sum " +
                "FROM transactions t JOIN transaction_types tt ON t.transaction_type_id = tt.id " +
                "WHERE t.user_id = ? AND tt.name = 'Поступление' AND " + getPeriodFilter(period) +
                " GROUP BY period ORDER BY period";
        return doubleSeriesCommon(sql, userId);
    }

    public List<ChartDoubleItem> getSumOutcome(String period, int userId) {
        String sql = "SELECT " + getPeriodAggregate(period) + " as period, SUM(amount) as sum " +
                "FROM transactions t JOIN transaction_types tt ON t.transaction_type_id = tt.id " +
                "WHERE t.user_id = ? AND tt.name = 'Списание' AND " + getPeriodFilter(period) +
                " GROUP BY period ORDER BY period";
        return doubleSeriesCommon(sql, userId);
    }

    public List<ChartLongItem> getCompletedTransactions(String period, int userId) {
        String sql = "SELECT " + getPeriodAggregate(period) + " as period, COUNT(*) as count " +
                "FROM transactions t JOIN transaction_statuses ts ON t.status_id = ts.id " +
                "WHERE t.user_id = ? AND ts.name = 'Платеж выполнен' AND " + getPeriodFilter(period) +
                " GROUP BY period ORDER BY period";
        return longSeriesCommon(sql, userId);
    }

    public List<ChartLongItem> getCancelledTransactions(String period, int userId) {
        String sql = "SELECT " + getPeriodAggregate(period) + " as period, COUNT(*) as count " +
                "FROM transactions t JOIN transaction_statuses ts ON t.status_id = ts.id " +
                "WHERE t.user_id = ? AND ts.name = 'Отменена' AND " + getPeriodFilter(period) +
                " GROUP BY period ORDER BY period";
        return longSeriesCommon(sql, userId);
    }

    public Map<String, Double> getBankIncomeStats(String period, int userId) {
        String sql = "SELECT receiver_bank, SUM(amount) FROM transactions WHERE user_id = ? AND " + getPeriodFilter(period) + " GROUP BY receiver_bank";
        return mapCommon(sql, userId);
    }

    public Map<String, Double> getBankOutcomeStats(String period, int userId) {
        String sql = "SELECT sender_bank, SUM(amount) FROM transactions WHERE user_id = ? AND " + getPeriodFilter(period) + " GROUP BY sender_bank";
        return mapCommon(sql, userId);
    }

    public Map<String, Double> getCategoryStats(String period, int userId) {
        String sql = "SELECT c.name, SUM(t.amount) FROM transactions t JOIN categories c ON t.category_id = c.id WHERE t.user_id = ? AND " + getPeriodFilter(period) + " GROUP BY c.name";
        return mapCommon(sql, userId);
    }

    private record ChartDoubleItem(String x, double y) {}
    private List<ChartDoubleItem> doubleSeriesCommon(String sql, int userId) {
        List<ChartDoubleItem> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new ChartDoubleItem(rs.getString(1), rs.getDouble(2)));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private record ChartLongItem(String x, long y) {}
    private List<ChartLongItem> longSeriesCommon(String sql, int userId) {
        List<ChartLongItem> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new ChartLongItem(rs.getString(1), rs.getLong(2)));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Double> mapCommon(String sql, int userId) {
        var res = new HashMap<String, Double>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    res.put(rs.getString(1), rs.getDouble(2));
                }
            }
            return res;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}