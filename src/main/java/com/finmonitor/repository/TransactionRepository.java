package com.finmonitor.repository;

import com.finmonitor.config.JDBCConnector;
import com.finmonitor.model.jdbc.Transaction;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionRepository {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public long save(Transaction t) {
        try (Connection conn = JDBCConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO transactions (person_type_id, transaction_type_id, status_id, category_id, transaction_datetime, comment, amount, sender_bank, receiver_bank, account_number, receiver_account_number, receiver_inn, receiver_phone) " +
                             "VALUES ((SELECT id FROM person_types WHERE name = ?)," +
                             "(SELECT id FROM transaction_types WHERE name = ?)," +
                             "(SELECT id FROM transaction_statuses WHERE name = ?)," +
                             "(SELECT id FROM categories WHERE name = ?)," +
                             "?, ?, ?, ?, ?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, t.getPersonType());
            stmt.setString(2, t.getTransactionType());
            stmt.setString(3, t.getStatus());
            if (t.getCategory() != null) {
                stmt.setString(4, t.getCategory());
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDate.parse(t.getDateTime(), formatter).atStartOfDay()));
            stmt.setString(6, t.getComment());
            stmt.setDouble(7, t.getAmount());
            stmt.setString(8, t.getSenderBank());
            stmt.setString(9, t.getReceiverBank());
            stmt.setString(10, t.getSenderAccountNumber());
            stmt.setString(11, t.getReceiverAccountNumber());
            stmt.setString(12, t.getReceiverINN());
            stmt.setString(13, t.getReceiverPhone());

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

    public List<Transaction> findByFilters(Map<String, String> filters) {
        List<Transaction> transactions = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM transactions_full_view WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (filters.containsKey("from_bank")) {
            sql.append(" AND sender_bank = ?");
            params.add(filters.get("from_bank"));
        }
        if (filters.containsKey("to_bank")) {
            sql.append(" AND receiver_bank = ?");
            params.add(filters.get("to_bank"));
        }
        if (filters.containsKey("from_date")) {
            sql.append(" AND transaction_datetime >= ?");
            params.add(filters.get("from_date"));
        }
        if (filters.containsKey("to_date")) {
            sql.append(" AND transaction_datetime <= ?");
            params.add(filters.get("to_date"));
        }
        if (filters.containsKey("status")) {
            sql.append(" AND status_id = (SELECT id FROM transaction_statuses WHERE status = ?)");
            params.add(filters.get("status"));
        }
        if (filters.containsKey("inn")) {
            sql.append(" AND receiver_inn = ?");
            params.add(filters.get("inn"));
        }
        if (filters.containsKey("amount_from")) {
            sql.append(" AND amount >= ?");
            params.add(Double.parseDouble(filters.get("amount_from")));
        }
        if (filters.containsKey("amount_to")) {
            sql.append(" AND amount <= ?");
            params.add(Double.parseDouble(filters.get("amount_to")));
        }
        if (filters.containsKey("type")) {
            sql.append(" AND transaction_type_id = (SELECT id FROM transaction_types WHERE transaction_type = ?)");
            params.add(filters.get("type"));
        }
        if (filters.containsKey("category")) {
            sql.append(" AND category_id = (SELECT id FROM categories WHERE category = ?)");
            params.add(filters.get("category"));
        }

        try (Connection conn = JDBCConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return transactions;
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
        t.setReceiverPhone(rs.getString("receiver_phone"));
        return t;
    }

    private String getPeriodAggregate(String period) {
        return switch (period) {
            case "W" -> "to_char(transaction_datetime, 'IYYY-IW')";
            case "M" -> "to_char(transaction_datetime, 'YYYY-MM')";
            case "Q" ->
                    "concat(extract(year FROM transaction_datetime), '-Q', extract(quarter FROM transaction_datetime))";
            case "Y" -> "to_char(transaction_datetime, 'YYYY')";
            default -> throw new IllegalArgumentException("Invalid period: " + period);
        };
    }

    private String getPeriodFilter(String period) {
        return switch (period) {
            case "W" -> "to_char(transaction_datetime, 'IYYY-IW') = to_char(now(), 'IYYY-IW')";
            case "M" -> "to_char(transaction_datetime, 'YYYY-MM') = to_char(now(), 'YYYY-MM')";
            case "Q" ->
                    "to_char(transaction_datetime, 'YYYY-MM') BETWEEN to_char(now() - INTERVAL '3 months', 'YYYY-MM') AND to_char(now(), 'YYYY-MM')";
            case "Y" -> "to_char(transaction_datetime, 'YYYY') = to_char(now(), 'YYYY')";
            default -> throw new IllegalArgumentException("Invalid period: " + period);
        };
    }

    public Transaction findById(long id) {
        String sql = "SELECT * FROM transactions_full_view WHERE id = ?";
        try (Connection conn = JDBCConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
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

    public void update(Transaction t) {
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
        WHERE id = ?
    """;

        try (Connection conn = JDBCConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, t.getPersonType());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDate.parse(t.getDateTime(), formatter).atStartOfDay()));
            stmt.setString(3, t.getComment());
            stmt.setDouble(4, t.getAmount());
            stmt.setString(5, t.getStatus());
            stmt.setString(6, t.getSenderBank());
            stmt.setString(7, t.getReceiverBank());
            stmt.setString(8, t.getReceiverINN());
            stmt.setString(9, t.getCategory());
            stmt.setString(10, t.getReceiverPhone());
            stmt.setLong(11, t.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating transaction", e);
        }
    }

    public void markAsDeleted(long id) {
        String sql = "UPDATE transactions SET status_id = (SELECT id FROM transaction_statuses WHERE name = 'Платеж удален') WHERE id = ?";
        try (Connection conn = JDBCConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error marking transaction as deleted", e);
        }
    }

    public void updateCategory(long id, String category) {
        String sql = "UPDATE transactions SET category_id = (SELECT id FROM categories WHERE name = ?) WHERE id = ?";
        try (Connection conn = JDBCConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating category", e);
        }
    }

    public String getReport() {
        StringBuilder csv = new StringBuilder();
        String query = "SELECT * FROM transactions_full_view";

        try (Connection conn = JDBCConnector.getConnection();
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

    public List<ChartLongItem> getTransactionCount(String period) {
        return longSeriesCommon("SELECT %s AS period, COUNT(*) FROM transactions GROUP BY period", period);
    }

    public List<ChartDoubleItem> getDebetCount(String period) {
        return doubleSeriesCommon("SELECT %s AS period, COUNT(*) FROM transactions WHERE transaction_type_id = (SELECT id FROM transaction_types WHERE name = 'Поступление') GROUP BY period", period);
    }

    public List<ChartDoubleItem> getCreditCount(String period) {
        return doubleSeriesCommon("SELECT %s AS period, COUNT(*) FROM transactions WHERE transaction_type_id = (SELECT id FROM transaction_types WHERE name = 'Списание') GROUP BY period", period);
    }

    public List<ChartDoubleItem> getSumIncome(String period) {
        return doubleSeriesCommon("SELECT %s AS period, SUM(amount) FROM transactions WHERE transaction_type_id = (SELECT id FROM transaction_types WHERE name = 'Поступление') GROUP BY period", period);
    }

    public List<ChartDoubleItem> getSumOutcome(String period) {
        return doubleSeriesCommon("SELECT %s AS period, SUM(amount) FROM transactions WHERE transaction_type_id = (SELECT id FROM transaction_types WHERE name = 'Списание') GROUP BY period", period);
    }

    public List<ChartLongItem> getCompletedTransactions(String period) {
        return longSeriesCommon("SELECT %s AS period, COUNT(*) FROM transactions WHERE status_id = (SELECT id FROM transaction_statuses WHERE name = 'Платеж выполнен') GROUP BY period", period);
    }

    public List<ChartLongItem> getCancelledTransactions(String period) {
        return longSeriesCommon("SELECT %s AS period, COUNT(*) FROM transactions WHERE status_id = (SELECT id FROM transaction_statuses WHERE name = 'Отменена') GROUP BY period", period);
    }

    public Map<String, Double> getBankIncomeStats(String period) {
        return mapCommon("SELECT receiver_bank, SUM(amount) FROM transactions WHERE %s GROUP BY receiver_bank", period);
    }

    public Map<String, Double> getBankOutcomeStats(String period) {
        return mapCommon("SELECT sender_bank, SUM(amount) FROM transactions WHERE %s GROUP BY sender_bank", period);
    }

    public Map<String, Double> getCategoryStats(String period) {
        return mapCommon("SELECT name, SUM(amount) FROM transactions JOIN categories ON category_id = categories.id WHERE %s GROUP BY name", period);
    }

    private record ChartDoubleItem(String x, double y) {}
    private List<ChartDoubleItem> doubleSeriesCommon(String req, String period) {
        String sql = req.formatted(getPeriodAggregate(period));
        var res = new ArrayList<ChartDoubleItem>();
        try (Connection conn = JDBCConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                res.add(new ChartDoubleItem(rs.getString(1), rs.getDouble(2)));
            }
            return res;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private record ChartLongItem(String x, double y) {}
    private List<ChartLongItem> longSeriesCommon(String req, String period) {
        String sql = req.formatted(getPeriodAggregate(period));
        var res = new ArrayList<ChartLongItem>();
        try (Connection conn = JDBCConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                res.add(new ChartLongItem(rs.getString(1), rs.getLong(2)));
            }
            return res;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Double> mapCommon(String req, String period) {
        String sql = req.formatted(getPeriodFilter(period));
        var res = new HashMap<String, Double>();
        try (Connection conn = JDBCConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                res.put(rs.getString(1), rs.getDouble(2));
            }
            return res;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}