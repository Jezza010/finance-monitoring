package com.finmonitor.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TemplateHandler implements HttpHandler {
    private static final String TEMPLATES_DIR = "src/main/resources/templates";
    private final String templateName;

    public TemplateHandler(String templateName) {
        this.templateName = templateName;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Path path = Paths.get(TEMPLATES_DIR, templateName);

        if (!Files.exists(path)) {
            String response = "404 Not Found";
            exchange.sendResponseHeaders(404, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, Files.size(path));

        try (OutputStream os = exchange.getResponseBody()) {
            Files.copy(path, os);
        }
    }
} 