package com.finmonitor.repository;

import com.finmonitor.model.Session;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.Optional;

public class SessionRepository {
    private final HikariDataSource dataSource;

    public SessionRepository(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(Session session) {
        String sql = "INSERT INTO sessions (user_id, session_token, created_at) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, session.getUserId());
            stmt.setString(2, session.getSessionToken());
            stmt.setTimestamp(3, Timestamp.from(session.getCreatedAt()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    session.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save session", e);
        }
    }

    public Optional<Session> findByToken(String token) {
        String sql = "SELECT id, user_id, session_token, created_at FROM sessions WHERE session_token = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(Session.builder()
                            .id(rs.getInt("id"))
                            .userId(rs.getInt("user_id"))
                            .sessionToken(rs.getString("session_token"))
                            .createdAt(rs.getTimestamp("created_at").toInstant())
                            .build());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find session by token", e);
        }
        return Optional.empty();
    }

    public void deleteByToken(String token) {
        String sql = "DELETE FROM sessions WHERE session_token = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete session", e);
        }
    }
} 