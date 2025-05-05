package com.finmonitor.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


@Slf4j
public class JDBCConnector {
    @Getter
    private static final HikariDataSource source;

    static {
        Properties properties = Config.getProperties();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getProperty("datasource.db.url"));
        config.setUsername(properties.getProperty("datasource.db.user"));
        config.setPassword(properties.getProperty("datasource.db.password"));
        config.setMaximumPoolSize(10);
        config.setMaxLifetime(600000);

        log.info("Connection Pool for data source '{}':", config.getJdbcUrl());

        source = new HikariDataSource(config);

        log.info("HirakiCP dataSource initialized");
    }

    private JDBCConnector() {}

    public static Connection getConnection() {
        Connection connection;
        try {
            connection = source.getConnection();
            log.info("HirakiCP Datasource connection to '{}' got in '{}'", connection.getCatalog(), source.getPoolName());
        } catch (SQLException e) {
            log.error("Failed to get connection from HikariCP DataSource...", e);
            throw new RuntimeException(e);
        }
        return connection;
    }
}
