package com.finmonitor.dao.jdbc;

import com.finmonitor.dao.Dao;
import com.finmonitor.model.Transaction;

import java.util.List;
import java.util.Optional;

public class TransactionJDBCDaoImpl implements Dao<Transaction, Long> {
    @Override
    public Transaction save(Transaction entity) {
        return null;
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
    public Optional<Transaction> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public List<Transaction> findAll() {
        return List.of();
    }
}
