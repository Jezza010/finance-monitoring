package com.finmonitor.http;

import com.finmonitor.model.Session;
import com.finmonitor.repository.SessionRepository;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;

public abstract class BaseHandler implements HttpHandler {
    protected final SessionRepository sessionRepo;
    protected final Gson gson = new Gson();

    protected BaseHandler(SessionRepository sessionRepo) {
        this.sessionRepo = sessionRepo;
    }

    protected Optional<Session> validateSession(HttpExchange exchange) {
        String sessionToken = getSessionToken(exchange);
        if (sessionToken == null) {
            sendResponse(exchange, 403, Map.of("error", "No session token provided"));
            return Optional.empty();
        }

        Optional<Session> session = sessionRepo.findByToken(sessionToken);
        if (session.isEmpty()) {
            sendResponse(exchange, 403, Map.of("error", "Invalid session token"));
            return Optional.empty();
        }

        return session;
    }

    protected String getSessionToken(HttpExchange ex) {
        String cookieHeader = ex.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader == null) return null;

        for (String cookie : cookieHeader.split(";")) {
            String[] parts = cookie.trim().split("=");
            if (parts.length == 2 && "session".equals(parts[0])) {
                return parts[1];
            }
        }
        return null;
    }

    protected void sendResponse(HttpExchange ex, int status, Object body) {
        try {
            String json = gson.toJson(body);
            ex.getResponseHeaders().set("Content-Type", "application/json");
            ex.sendResponseHeaders(status, json.getBytes().length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(json.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 