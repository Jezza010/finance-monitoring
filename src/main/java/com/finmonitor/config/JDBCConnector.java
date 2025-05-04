package com.finmonitor.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


@Slf4j
public class JDBCConnector {

    private static final HikariDataSource source;

    static {
        Properties properties = new Properties();
        try (InputStream reader = ClassLoader.getSystemResourceAsStream("application.properties")) {
            properties.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getProperty("datasource.db.url"));
        config.setUsername(properties.getProperty("datasource.db.user"));
        config.setPassword(properties.getProperty("datasource.db.password"));
        config.setMaximumPoolSize(10);
        config.setMaxLifetime(600000);

        System.out.printf("Connection Pool for data source '%s'\n", config.getJdbcUrl());

        source = new HikariDataSource(config);
        log.info("HirakiCP dataSource initialized");
    }

    private JDBCConnector() {}

    public static Connection getConnection() {
        Connection connection;
        try {
            connection = source.getConnection();
            log.info("HirakiCP Datasource connection got!");
        } catch (SQLException e) {
            log.error("Failed to get connection from HikariCP DataSource...", e);
            throw new RuntimeException(e);
        }
        return connection;
    }

    public static HikariDataSource getDataSource() {
        return source;
    }

}
