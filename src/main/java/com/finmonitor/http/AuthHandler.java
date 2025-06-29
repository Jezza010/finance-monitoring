package com.finmonitor.http;

import com.finmonitor.model.Session;
import com.finmonitor.model.User;
import com.finmonitor.repository.SessionRepository;
import com.finmonitor.repository.UserRepository;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


// регистрацию и вход без токенов
@Slf4j
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

            Function<Integer, String> badRequestLog = status -> gson.toJson(Map.of(
                    "method", method.toUpperCase(),
                    "endpoint_path", path,
                    "status", status));

            if ("POST".equals(method)) {
                if (path.endsWith("/register")) {
                    handleRegister(exchange);
                } else if (path.endsWith("/login")) {
                    handleLogin(exchange);
                } else if (path.endsWith("/logout")) {
                    handleLogout(exchange);
                } else {
                    String msg = "Not found";
                    sendResponse(exchange, 404, Map.of("error", msg));
                    log.warn("Invalid request endpoint path {} ... {}", badRequestLog.apply(404), msg);
                }
            } else {
                String msg = "Method not allowed";
                sendResponse(exchange, 405, Map.of("error", msg));
                log.warn("Invalid request {} ... {}", badRequestLog.apply(405), msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Internal server error";
            sendResponse(exchange, 500, Map.of("error", msg));
            log.error("Failed request... {} (500):\t{}", msg, e.getMessage());
        }
    }

    // Регистрация нового пользователя
    private void handleRegister(HttpExchange ex) {
        try (InputStreamReader reader = new InputStreamReader(ex.getRequestBody(), StandardCharsets.UTF_8)) {
            Map<String, String> request = gson.fromJson(reader, Map.class);
            String username = request.get("username");
            String password = request.get("password");

            Function<Integer, String> badUserLog = status -> gson.toJson(Map.of(
                "username", username,
                "status", status));

            if (username == null || password == null) {
                String msg = "Username and password are required";
                sendResponse(ex, 400, Map.of("error", msg));
                log.warn("User {} not registered...{}", badUserLog.apply(400), msg);
                return;
            }

            if (userRepo.findByUsername(username).isPresent()) {
                String msg = "Username already exists";
                sendResponse(ex, 400, Map.of("error", msg));
                log.warn("User {} not registered...{}", badUserLog.apply(400), msg);
                return;
            }

            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
            userRepo.save(user);

//             sendResponse(ex, 200, Map.of("success", true));

            var newUser = userRepo.findByUsername(username);

            if (newUser.isPresent()) {
                sendResponse(ex, 200, Map.of("success", true));
                log.info("User {} registered", gson.toJson(Map.of(
                        "id", newUser.get().getId(),
                        "username", username,
                        "status", 200)));
            } else {
                String msg = "Not recorded into db (internal server error)";
                sendResponse(ex, 500, Map.of("error", msg));
                log.error("User {} not registered...{}", badUserLog.apply(500), msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
            String msg = "Internal server error";
            sendResponse(ex, 500, Map.of("error", msg));
            log.error("User not registered... {} (500):\t{}", msg, e.getMessage());
        }
    }

    // вход по логину и паролю
    private void handleLogin(HttpExchange ex) {
        try (InputStreamReader reader = new InputStreamReader(ex.getRequestBody(), StandardCharsets.UTF_8)) {
            Map<String, String> request = gson.fromJson(reader, Map.class);
            String username = request.get("username");
            String password = request.get("password");

            Function<Integer, String> badUserLog = status -> gson.toJson(Map.of(
                    "username", username,
                    "status", status));

            if (username == null || password == null) {
                String msg = "Username and password are required";
                sendResponse(ex, 400, Map.of("error", msg));
                log.warn("\"User {} cannot log in...{}", badUserLog.apply(400), msg);
                return;
            }

            Optional<User> userOpt = userRepo.findByUsername(username);
            if (userOpt.isEmpty() || !BCrypt.checkpw(password, userOpt.get().getPasswordHash())) {
                String msg = "UInvalid username or passwordd";
                sendResponse(ex, 401, Map.of("error", msg));
                log.warn("\"User {} cannot log in...{}", badUserLog.apply(401), msg);
                return;
            }

            User user = userOpt.get();
            String sessionToken = UUID.randomUUID().toString();
            Session session = Session.builder()
                    .userId(user.getId())
                    .sessionToken(sessionToken)
                    .createdAt(Instant.now())
                    .build();
            sessionRepo.save(session);

//             String cookie = String.format("session=%s; Path=/; HttpOnly", sessionToken);
//             ex.getResponseHeaders().add("Set-Cookie", cookie);
//             sendResponse(ex, 200, Map.of("message", "Login successful", "username", username));

            var newSession = sessionRepo.findByToken(sessionToken);

            if (newSession.isPresent()) {
                String cookie = String.format("session=%s; Path=/; HttpOnly", sessionToken);
                ex.getResponseHeaders().add("Set-Cookie", cookie);
                sendResponse(ex, 200, Map.of("message", "Login successful", "username", username));
                log.info("User {} log in", gson.toJson(Map.of(
                        "id", userOpt.get().getId(),
                        "username", username,
                        "status", 200)));
            } else {
                String msg = "The session was not saved to the database (internal server error)";
                sendResponse(ex, 500, Map.of("error", msg));
                log.error("User {} cannot log in...{}", badUserLog.apply(500), msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
            String msg = "Internal server error";
            sendResponse(ex, 500, Map.of("error", msg));
            log.error("The user cannot log in.... {} (500):\t{}", msg, e.getMessage());
        }
    }

    private void handleLogout(HttpExchange ex) {
        String sessionToken = getSessionToken(ex);
//        if (sessionToken != null) {
//            sessionRepo.deleteByToken(sessionToken);
//        }
//
//        String cookie = "session=; Path=/; HttpOnly; Max-Age=0";
//        ex.getResponseHeaders().add("Set-Cookie", cookie);
//        sendResponse(ex, 200, Map.of("message", "Logged out successfully"));

        Consumer<String> logoutOk = cookie -> {
            if (cookie == null || cookie.isEmpty()) cookie = "session=; Path=/; HttpOnly; Max-Age=0";
            ex.getResponseHeaders().add("Set-Cookie", cookie);
            sendResponse(ex, 200, Map.of("message", "Logged out successfully"));
        };

        Function<Boolean, Boolean> logoutWithoutSession = hasNotSession -> {
            if (hasNotSession) {
                logoutOk.accept("");
                log.info("User log out");
            }
            return hasNotSession;
        };

        if (logoutWithoutSession.apply(sessionToken == null)) return;
        var session = sessionRepo.findByToken(sessionToken);
        if (logoutWithoutSession.apply(session.isEmpty())) return;

        Function<Integer, String> sessionLog = status -> gson.toJson(Map.of(
                "id", session.get().getUserId(),
                "status", status));

        sessionRepo.deleteByToken(sessionToken);

        if (sessionRepo.findByToken(sessionToken).isPresent()) {
            String msg = "The session was not deleted (internal server error)";
            sendResponse(ex, 500, Map.of("error", msg));
            log.error("User {} cannot log out...{}", sessionLog.apply(500), msg);
        } else {
            logoutOk.accept("");
            log.info("User {} log out", sessionLog.apply( 200));
        }
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
            log.error("Exchange by request {}:\n\t{}",
                    gson.toJson(Map.of(
                            "method", ex.getRequestMethod(),
                            "endpoint_path", ex.getRequestURI().getPath(),
                            "status", 500)),
                    Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
}
