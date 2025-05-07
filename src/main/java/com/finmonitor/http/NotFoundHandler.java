package com.finmonitor.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NotFoundHandler implements HttpHandler {
    private static final String TEMPLATES_DIR = "src/main/resources/templates";
    private static final String ERROR_TEMPLATE = "404.html";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Path path = Paths.get(TEMPLATES_DIR, ERROR_TEMPLATE);

        if (!Files.exists(path)) {
            String response = "404 Not Found";
            exchange.sendResponseHeaders(404, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(404, Files.size(path));

        try (OutputStream os = exchange.getResponseBody()) {
            Files.copy(path, os);
        }
    }
} 