
--CREATE DATABASE finance_db;

-- Добавляем администраторов БД
CREATE USER admin WITH PASSWORD 'admin'; -- укажите ваш пароль
-- для admin - все права на таблицы
GRANT ALL PRIVILEGES ON DATABASE finance_db TO admin;

\connect finance_db;

-- Добавляем схемы
CREATE SCHEMA IF NOT EXISTS finance_monitoring AUTHORIZATION admin; --postgres;
-- для admin - все права на таблицы в схеме
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA finance_monitoring TO admin;

-- Схема по умолчанию
ALTER DATABASE finance_db SET SEARCH_PATH TO finance_monitoring;
ALTER USER admin SET SEARCH_PATH TO finance_monitoring;
