package com.finmonitor.repository;

import com.finmonitor.config.JDBCConnector;
import com.finmonitor.model.User;
import java.sql.*;
import java.util.Optional;


//класс для работы с таблицей user
// здесь мы будем сохранять нового пользователя и искать его по имени

public class UserRepository {
    public Optional<User> findByUsername(String username) {
        // sql запрос, который ищет запись по полю username
        String sql = "SELECT id, username, password_hash, created_at FROM users WHERE username = ?";

        try (
                Connection conn = JDBCConnector.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            // Подставляем username вместо "?" !!!
            stmt.setString(1, username);

            // выполнение самгго запроса
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // если запись найдена - создаем объект User из полей
                    User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("created_at")
                    );
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            // пока что время разработки просто печатаем стек и возвращаем пустой Optional  !!!
            e.printStackTrace();
        }

        // Если ничего не нашли или упало исключение — возвращаем пустоту
        return Optional.empty();
    }

    public void save(User user) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (
            Connection conn = JDBCConnector.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            // Подставляем логин и хеш пароля
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());

            stmt.executeUpdate();

            // получаем автоматически сгенерированный id и сохраняем его в объект
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
