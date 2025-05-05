CREATE DATABASE finance_db;

-- Справочник: Типы лиц
CREATE TABLE person_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Справочник: Типы транзакций
CREATE TABLE transaction_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Справочник: Статусы транзакций
CREATE TABLE transaction_statuses (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Справочник: Категории
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_categories_name_user ON categories(name, user_id);

-- Основная таблица: Транзакции
CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    person_type_id INTEGER NOT NULL REFERENCES person_types(id),
    transaction_type_id INTEGER NOT NULL REFERENCES transaction_types(id),
    transaction_datetime TIMESTAMP NOT NULL,
    comment TEXT,
    amount DECIMAL(15,2) NOT NULL,
    status_id INTEGER NOT NULL REFERENCES transaction_statuses(id),
    sender_bank VARCHAR(255),
    receiver_bank VARCHAR(255),
    account_number VARCHAR(50),
    receiver_account_number VARCHAR(50),
    receiver_inn VARCHAR(12),
    receiver_phone VARCHAR(20),
    category_id INTEGER REFERENCES categories(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);

CREATE VIEW transactions_full_view AS
SELECT
    t.id,
    t.user_id,
    pt.name as person_type,
    tt.name as transaction_type,
    t.transaction_datetime,
    t.comment,
    t.amount,
    ts.name as status,
    t.sender_bank,
    t.receiver_bank,
    t.account_number,
    t.receiver_account_number,
    t.receiver_inn,
    t.receiver_phone,
    c.name as category
FROM
    transactions t
JOIN
    person_types pt ON t.person_type_id = pt.id
JOIN
    transaction_types tt ON t.transaction_type_id = tt.id
JOIN
    transaction_statuses ts ON t.status_id = ts.id
LEFT JOIN
    categories c ON t.category_id = c.id;


-- Таблица пользователей

CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица активных сессий 
CREATE TABLE sessions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_token VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sessions_token ON sessions(session_token); 