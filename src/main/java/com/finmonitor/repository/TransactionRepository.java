package com.finmonitor.repository;

import com.finmonitor.model.Transaction;
import com.finmonitor.util.DatabaseConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {
    public List<Transaction> findAll() {
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM transactions")) {

            while (rs.next()) {
                Transaction t = new Transaction();
                t.id = rs.getLong("id");
                t.personType = rs.getString("person_type");
                t.transactionType = rs.getString("transaction_type");
                t.dateTime = rs.getString("date_time");
                t.comment = rs.getString("comment");
                t.amount = rs.getDouble("amount");
                t.status = rs.getString("status");
                t.senderBank = rs.getString("sender_bank");
                t.receiverBank = rs.getString("receiver_bank");
                t.receiverINN = rs.getString("receiver_inn");
                t.receiverAccount = rs.getString("receiver_account");
                t.category = rs.getString("category");
                t.phone = rs.getString("phone");
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void save(Transaction t) {
        try (Connection conn = DatabaseConnector.connect()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO transactions (person_type, transaction_type, date_time, comment, amount, status, sender_bank, receiver_bank, receiver_inn, receiver_account, category, phone) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            stmt.setString(1, t.personType);
            stmt.setString(2, t.transactionType);
            stmt.setString(3, t.dateTime);
            stmt.setString(4, t.comment);
            stmt.setDouble(5, t.amount);
            stmt.setString(6, t.status);
            stmt.setString(7, t.senderBank);
            stmt.setString(8, t.receiverBank);
            stmt.setString(9, t.receiverINN);
            stmt.setString(10, t.receiverAccount);
            stmt.setString(11, t.category);
            stmt.setString(12, t.phone);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}