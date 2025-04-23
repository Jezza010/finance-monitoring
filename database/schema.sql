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
