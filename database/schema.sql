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
    name VARCHAR(100) UNIQUE NOT NULL
);

-- Основная таблица: Транзакции
CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    person_type_id INT NOT NULL REFERENCES person_types(id),
    transaction_type_id INT NOT NULL REFERENCES transaction_types(id),
    status_id INT NOT NULL REFERENCES transaction_statuses(id),
    category_id INT REFERENCES categories(id),
    transaction_datetime TIMESTAMP NOT NULL,
    comment TEXT,
    amount NUMERIC(15, 5) CHECK (amount >= 0),
    sender_bank VARCHAR(255),
    receiver_bank VARCHAR(255),
    account_number VARCHAR(34),
    receiver_account_number VARCHAR(34),
    receiver_inn VARCHAR(11) CHECK (receiver_inn ~ '^\d{11}$'),
    receiver_phone VARCHAR(16) CHECK (receiver_phone ~ '^(\+7|8)\d{10}$')
);

CREATE VIEW transactions_full_view AS
SELECT
    t.id,

    -- заменяем person_type_id на имя
    pt.name AS person_type,

    -- заменяем transaction_type_id на имя
    tt.name AS transaction_type,

    -- заменяем status_id на имя
    ts.name AS status,

    -- заменяем category_id на имя (может быть NULL)
    c.name AS category,

    -- остальные поля как есть
    t.transaction_datetime,
    t.comment,
    t.amount,
    t.sender_bank,
    t.receiver_bank,
    t.account_number,
    t.receiver_account_number,
    t.receiver_inn,
    t.receiver_phone

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