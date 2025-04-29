package com.finmonitor.model;

public class User {
    private int id;     
    private String username; 
    private String passwordHash;
    private String createdAt;

    // пустой конструктор для JDBC (?)
    public User() { }

    // конструктор для нового пользователя (до сохранения в БД)
    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    // уже поолный конструктор (когда все читаем из БД)
    public User(int id, String username, String passwordHash, String createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
