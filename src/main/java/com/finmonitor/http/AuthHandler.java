package com.finmonitor.http;

import com.finmonitor.model.User;
import com.finmonitor.repository.UserRepository;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mindrot.jbcrypt.BCrypt;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;


// регистрацию и вход без токенов

public class AuthHandler implements HttpHandler {
    private final UserRepository userRepo = new UserRepository();
    private final Gson gson = new Gson();

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
        Map data = gson.fromJson(reader, Map.class);
        String username = (String) data.get("username");
        String password = (String) data.get("password");

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
        Map data = gson.fromJson(reader, Map.class);
        String username = (String) data.get("username");
        String password = (String) data.get("password");

        Optional<User> opt = userRepo.findByUsername(username);
        if (opt.isEmpty() || !BCrypt.checkpw(password, opt.get().getPasswordHash())) {
            sendResponse(ex, 401, Map.of("error", "Invalid username or password"));
            return;
        }

        // подтверждение
        sendResponse(ex, 200, Map.of("message", "Login successful", "username", username));
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
