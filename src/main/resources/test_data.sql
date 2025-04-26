-- Назначение: Описывает юридический статус владельца транзакции.
INSERT INTO storage.person_type (
    id, person_type_name)
    VALUES
    (1, 'Физическое лицо'),
    (2, 'Юридическое лицо')
;

-- Назначение: Описывает возможные типы операций.
INSERT INTO storage.transaction_type (
    id, transaction_type_name, sign_operator)
    VALUES
    (1, 'Списание со счёта', '-'),
    (2, 'Пополнение счёта', '+')
;

-- Назначение: Описывает возможные статусы операций.
INSERT INTO storage.transaction_status (
    id, transaction_status_name, is_immutable, is_completed, is_successful, is_deleted)
    VALUES
    (1, 'Новая',             false,  false,  false,  false),
    (2, 'Подтвержденная',    true,   false,  false,  false),
    (3, 'В обработке',       true,   false,  false,  false),
    (4, 'Отменена',          true,   true,   false,  false),
    (5, 'Платеж выполнен',   true,   true,   true,   false),
    (6, 'Платеж удален',     true,   true,   false,  false),
    (7, 'Возврат',           true,   true,   false,  false)
;

-- Назначение: Описывает категорию ?!!
INSERT INTO storage.category (
    id, category_name)
    VALUES
    (1, 'Категория-1')
;
