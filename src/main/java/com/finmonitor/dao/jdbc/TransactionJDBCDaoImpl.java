package com.finmonitor.dao.jdbc;

import com.finmonitor.config.JDBCConnector;
import com.finmonitor.dao.Dao;
import com.finmonitor.model.jdbc.Transaction;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionJDBCDaoImpl implements Dao<Transaction, Long> {
    @Override
    public Transaction save(Transaction t) {
        final Date date;
        try (Connection conn = JDBCConnector.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO storage.transaction (" +
                    "id, transaction_datetime, " +
                    "sum, comment, sender_bank, sender_account_number, " +
                    "receiver_inn, receiver_bank, receiver_account_number, receiver_phone, " +
                    "person_type_id, transaction_type_id, transaction_status_id, category_id, user_id)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            int i = 0;
            stmt.setLong(++i, t.getId());
            stmt.setDate(++i, t.getTransaction_datetime());
            stmt.setDouble(++i, t.getSum());
            stmt.setString(++i, t.getComment());
            stmt.setString(++i, t.getSenderBank());
            stmt.setString(++i, t.getSenderAccountNumber());
            stmt.setString(++i, t.getReceiverBank());
            stmt.setString(++i, t.getReceiverAccountNumber());
            stmt.setString(++i, t.getReceiverPhone());
            stmt.setString(++i, t.getPersonTypeId());
            stmt.setString(++i, t.getTransactionTypeId());
            stmt.setString(++i, t.getTransactionStatusId());
            stmt.setString(++i, t.getCategoryId());
            stmt.setLong(++i, t.getUserId());

            date = (Date) Date.from(Instant.now());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (t.id != null) {
            Optional<Transaction> result = findById(t.id);
            if (result.isEmpty()) throw new RuntimeException("Transaction with id " + t.id + " not updated");
            return result.get();
        }

        return findAllByUserIdByDate(t.getUserId(), date).get(0);
    }

    @Override
    public Transaction update(Transaction entity) {
        return null;
    }

    @Override
    public boolean delete(Long aLong) {
        return false;
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return Optional.of(getTransactionsBySQL("SELECT * FROM storage.transaction" +
                " WHERE id = " + id +
                " LIMIT 1").get(0));
    }

    @Override
    public List<Transaction> findAll() {
        return getTransactionsBySQL("SELECT * FROM storage.transaction");
    }

    public List<Transaction> findAllByUserId(Long userId) {
        return getTransactionsBySQL("SELECT * FROM storage.transaction WHERE user_id = " + userId);
    }

    public List<Transaction> findAllByUserIdByDate(Long userId, Date date) {
        return getTransactionsBySQL("SELECT * FROM storage.transaction" +
                " WHERE user_id = " + userId +
                " AND date >= " + date +
                " SORT ON transaction_datetime");
    }

    public List<Transaction> getTransactionsBySQL(final String query) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = JDBCConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)
        ) {
            while (rs.next()) {
                try {
                    transactions.add(Transaction.builder()
                            .id(rs.getLong("id"))
                            .transaction_datetime(rs.getDate("transaction_datetime"))
                            .sum(rs.getDouble("sum"))
                            .comment(rs.getString("comment"))
                            .senderBank(rs.getString("sender_bank"))
                            .senderAccountNumber(String.valueOf(rs.getLong("sender_account_number")))
                            .receiverINN(rs.getString("receiver_INN"))
                            .receiverBank(rs.getString("receiver_bank"))
                            .receiverAccountNumber(String.valueOf(rs.getLong("receiver_account_number")))
                            .receiverPhone(String.valueOf(rs.getLong("receiver_phone")))
                            .personTypeId(rs.getString("person_type_id"))
                            .transactionTypeId(rs.getString("transaction_type_id"))
                            .transactionStatusId(rs.getString("transaction_status_id"))
                            .categoryId(rs.getString("category_id"))
                            .build());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return transactions;
    }
}
