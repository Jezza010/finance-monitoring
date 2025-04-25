--CREATE DATABASE finance_db;

-- Добавляем администратора БД
CREATE USER admin WITH PASSWORD 'admin';
GRANT ALL PRIVILEGES ON DATABASE finance_db TO admin;
\connect finance_db;

-- Добавляем схему storage
create schema if not exists storage authorization admin; --postgres;

-- ------------------------------------------------------------------

-- Справочник: Типы лиц
CREATE TABLE IF NOT EXISTS storage.person_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Справочник: Типы транзакций
CREATE TABLE IF NOT EXISTS storage.transaction_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Справочник: Статусы транзакций
CREATE TABLE IF NOT EXISTS storage.transaction_statuses (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Справочник: Категории
CREATE TABLE IF NOT EXISTS storage.categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

-- Основная таблица: Транзакции
CREATE TABLE IF NOT EXISTS storage.transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_datetime TIMESTAMP NOT NULL,
    comment TEXT,
    amount NUMERIC(15, 5) CHECK (amount >= 0),
    sender_bank VARCHAR(255),
    receiver_bank VARCHAR(255),
    account_number VARCHAR(34),
    receiver_account_number VARCHAR(34),
    receiver_inn VARCHAR(11) CHECK (receiver_inn ~ '^\d{11}$'),
    receiver_phone VARCHAR(16) CHECK (receiver_phone ~ '^(\+7|8)\d{10}$'),
    person_type_id INT NOT NULL,
    transaction_type_id INT NOT NULL,
    status_id INT NOT NULL,
    category_id INT NOT NULL,
    FOREIGN KEY (person_type_id) REFERENCES storage.person_types (id),
    FOREIGN KEY (transaction_type_id) REFERENCES storage.transaction_types (id),
    FOREIGN KEY (status_id) REFERENCES storage.transaction_statuses (id),
    FOREIGN KEY (category_id) REFERENCES storage.categories (id)
);


-- ------------------------------------------------------------------

-- для admin - все права на таблицы в схеме storage
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA storage TO admin;
