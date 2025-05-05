package com.finmonitor;

import com.finmonitor.config.Config;
import com.finmonitor.config.JDBCConnector;
import com.finmonitor.http.AuthHandler;
import com.finmonitor.http.TransactionHandler;
import com.finmonitor.repository.SessionRepository;
import com.finmonitor.repository.TransactionRepository;
import com.finmonitor.repository.UserRepository;
import com.sun.net.httpserver.HttpServer;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;


@Slf4j
public class App {
    public static void main(String[] args) throws IOException {
        HikariDataSource dataSource = JDBCConnector.getDataSource();
        
        try (Connection conn = dataSource.getConnection()) {
            log.info("Successful test connection to db '{}'\n", conn.getCatalog());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Initialize repositories
        UserRepository userRepo = new UserRepository(dataSource);
        SessionRepository sessionRepo = new SessionRepository(dataSource);
        TransactionRepository transactionRepo = new TransactionRepository(dataSource);

        int port = Config.getHttpServerPort();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Auth handler
        AuthHandler authHandler = new AuthHandler(userRepo, sessionRepo);
        server.createContext("/api/auth/register", authHandler);
        server.createContext("/api/auth/login", authHandler);
        server.createContext("/api/auth/logout", authHandler);

        // Transaction handler
        TransactionHandler transactionHandler = new TransactionHandler(sessionRepo, transactionRepo);
        server.createContext("/api/transaction", transactionHandler::transaction);
        server.createContext("/api/transactions_count", transactionHandler::transactionsCount);
        server.createContext("/api/debet_count", transactionHandler::debetCount);
        server.createContext("/api/credit_count", transactionHandler::creditCount);
        server.createContext("/api/sum_income", transactionHandler::sumIncome);
        server.createContext("/api/sum_outcome", transactionHandler::sumOutcome);
        server.createContext("/api/completed_transactions", transactionHandler::completedTransactions);
        server.createContext("/api/cancelled_transactions", transactionHandler::cancelledTransactions);
        server.createContext("/api/bank_income_stats", transactionHandler::bankIncomeStats);
        server.createContext("/api/bank_outcome_stats", transactionHandler::bankOutcomeStats);
        server.createContext("/api/category_stats", transactionHandler::categoryStats);
        server.createContext("/api/update_transaction", transactionHandler::updateTransaction);
        server.createContext("/api/delete_transaction", transactionHandler::deleteTransaction);
        server.createContext("/api/create_category", transactionHandler::createCategory);
        server.createContext("/api/update_category", transactionHandler::updateCategory);
        server.createContext("/api/export", transactionHandler::exportReport);

        server.setExecutor(null);

        server.start();

        log.info("The server is running on port {}...", port);
    }
}
