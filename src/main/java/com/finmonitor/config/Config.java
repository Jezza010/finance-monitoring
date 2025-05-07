package com.finmonitor.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


@Slf4j
public class Config {

    @Getter
    private static final Properties properties;

    static {
        String msg = "load application properties file .......";
        properties = new Properties();

        try (InputStream reader = ClassLoader.getSystemResourceAsStream("application.properties")) {
            properties.load(reader);
            log.info("{} Ok", msg);
        } catch (IOException e) {
            log.info("{} Fail", msg);
            throw new RuntimeException(e);
        }
    }

    private Config() {}

    public static int getHttpServerPort() {
        int port = 8080;
        try {
            port = Integer.parseInt(properties.getProperty("http.server.port"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return port;
    }
}
