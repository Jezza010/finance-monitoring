--CREATE DATABASE finance_db;

-- Добавляем администратора БД
CREATE USER admin WITH PASSWORD 'admin';
GRANT ALL PRIVILEGES ON DATABASE finance_db TO admin;
\connect finance_db;

-- Добавляем схему storage
create schema if not exists storage authorization admin; --postgres;

-- ------------------------------------------------------------------

-- Пользователи
CREATE TABLE IF NOT EXISTS storage.user (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(30) UNIQUE NOT NULL,
    password VARCHAR(30) NOT NULL,
    removed BOOLEAN
);

-- Справочник: Типы лиц
CREATE TABLE IF NOT EXISTS storage.person_type (
    id SERIAL PRIMARY KEY,
    person_type_name VARCHAR(50) UNIQUE NOT NULL,
    removed BOOLEAN
);

-- Справочник: Типы транзакций
CREATE TABLE IF NOT EXISTS storage.transaction_type (
    id SERIAL PRIMARY KEY,
    transaction_type_name VARCHAR(50) UNIQUE NOT NULL,
    sign_operator VARCHAR(1) UNIQUE NOT NULL,
    removed BOOLEAN
);

-- Справочник: Статусы транзакций
CREATE TABLE IF NOT EXISTS storage.transaction_status (
    id SERIAL PRIMARY KEY,
    transaction_status_name VARCHAR(50) UNIQUE NOT NULL,
    is_immutable BOOLEAN,
    is_completed BOOLEAN,
    is_successful BOOLEAN,
    is_deleted BOOLEAN,
    removed BOOLEAN
);

-- Справочник: Категории
CREATE TABLE IF NOT EXISTS storage.category (
    id SERIAL PRIMARY KEY,
    category_name VARCHAR(100) UNIQUE NOT NULL,
    removed BOOLEAN
);

-- Основная таблица: Транзакции
CREATE TABLE IF NOT EXISTS storage.transaction (
    id BIGSERIAL PRIMARY KEY,
    transaction_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sum NUMERIC(15, 5) CHECK (sum >= 0),
    comment TEXT,
    sender_bank VARCHAR(255),
    sender_account_number VARCHAR(34),
    receiver_inn VARCHAR(11) CHECK (receiver_inn ~ '^\d{11}$'),
    receiver_bank VARCHAR(255),
    receiver_account_number VARCHAR(34),
    receiver_phone VARCHAR(16) CHECK (receiver_phone ~ '^(\+7|8)\d{10}$'),
    person_type_id INT NOT NULL,
    transaction_type_id INT NOT NULL,
    status_id INT NOT NULL,
    category_id INT NOT NULL,
    user_id INT NOT NULL,
    FOREIGN KEY (person_type_id) REFERENCES storage.person_type (id),
    FOREIGN KEY (transaction_type_id) REFERENCES storage.transaction_type (id),
    FOREIGN KEY (status_id) REFERENCES storage.transaction_status (id),
    FOREIGN KEY (category_id) REFERENCES storage.category (id),
    FOREIGN KEY (user_id) REFERENCES storage.user (id)
);


-- ------------------------------------------------------------------

-- для admin - все права на таблицы в схеме storage
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA storage TO admin;
