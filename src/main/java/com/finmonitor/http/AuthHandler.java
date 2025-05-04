package com.finmonitor.http;

import com.finmonitor.model.Session;
import com.finmonitor.model.User;
import com.finmonitor.repository.SessionRepository;
import com.finmonitor.repository.UserRepository;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mindrot.jbcrypt.BCrypt;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


// регистрацию и вход без токенов

public class AuthHandler implements HttpHandler {
    private final UserRepository userRepo;
    private final SessionRepository sessionRepo;
    private final Gson gson = new Gson();

    public AuthHandler(UserRepository userRepo, SessionRepository sessionRepo) {
        this.userRepo = userRepo;
        this.sessionRepo = sessionRepo;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("POST".equals(method)) {
                if (path.endsWith("/register")) {
                    handleRegister(exchange);
                } else if (path.endsWith("/login")) {
                    handleLogin(exchange);
                } else if (path.endsWith("/logout")) {
                    handleLogout(exchange);
                } else {
                    sendResponse(exchange, 404, Map.of("error", "Not found"));
                }
            } else {
                sendResponse(exchange, 405, Map.of("error", "Method not allowed"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, Map.of("error", "Server error"));
        }
    }

    // Регистрация нового пользователя
    private void handleRegister(HttpExchange ex) {
        InputStreamReader reader = new InputStreamReader(ex.getRequestBody(), StandardCharsets.UTF_8);
        Map<String, String> data = gson.fromJson(reader, Map.class);
        String username = data.get("username");
        String password = data.get("password");

        // проверка есть ли уже такое имя
        if (userRepo.findByUsername(username).isPresent()) {
            sendResponse(ex, 409, Map.of("error", "Username already taken"));
            return;
        }

        // хэшируем пароль и сохраняем пользователя
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User(username, hash);
        userRepo.save(user);

        sendResponse(ex, 201, Map.of("message", "User registered"));
    }

    // вход по логину и паролю
    private void handleLogin(HttpExchange ex) {
        InputStreamReader reader = new InputStreamReader(ex.getRequestBody(), StandardCharsets.UTF_8);
        Map<String, String> data = gson.fromJson(reader, Map.class);
        String username = data.get("username");
        String password = data.get("password");

        Optional<User> opt = userRepo.findByUsername(username);
        if (opt.isEmpty() || !BCrypt.checkpw(password, opt.get().getPasswordHash())) {
            sendResponse(ex, 401, Map.of("error", "Invalid username or password"));
            return;
        }

        User user = opt.get();
        String sessionToken = UUID.randomUUID().toString();
        Session session = Session.builder()
                .userId(user.getId())
                .sessionToken(sessionToken)
                .createdAt(Instant.now())
                .build();
        sessionRepo.save(session);

        String cookie = String.format("session=%s; Path=/; HttpOnly", sessionToken);
        ex.getResponseHeaders().add("Set-Cookie", cookie);
        sendResponse(ex, 200, Map.of("message", "Login successful", "username", username));
    }

    private void handleLogout(HttpExchange ex) {
        String sessionToken = getSessionToken(ex);
        if (sessionToken != null) {
            sessionRepo.deleteByToken(sessionToken);
        }

        String cookie = "session=; Path=/; HttpOnly; Max-Age=0";
        ex.getResponseHeaders().add("Set-Cookie", cookie);
        sendResponse(ex, 200, Map.of("message", "Logged out successfully"));
    }

    private String getSessionToken(HttpExchange ex) {
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

    private void sendResponse(HttpExchange ex, int status, Object body) {
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
