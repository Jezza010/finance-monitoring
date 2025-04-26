-- Назначение: Описывает юридический статус владельца транзакции.
INSERT INTO storage.person_type (
    id, person_type_name, operator)
    VALUES
    (, 'Физическое лицо'),
    (, 'Юридическое лицо')
;

-- Назначение: Описывает возможные типы операций.
INSERT INTO storage.transaction_type (
    id, transaction_type_name, operator)
    VALUES
    (, 'Списание со счёта', '-'),
    (, 'Пополнение счёта', '+')
;

-- Назначение: Описывает возможные статусы операций.
INSERT INTO storage.transaction_status (
    id, transaction_status_name, is_immutable, is_completed, is_successful, isDeleted)
    VALUES
    (, 'Новая',             false,  false,  false,  false),
    (, 'Подтвержденная',    true,   false,  false,  false),
    (, 'В обработке',       true,   false,  false,  false),
    (, 'Отменена',          true,   true,   false,  false),
    (, 'Платеж выполнен',   true,   true,   true,   false),
    (, 'Платеж удален',     true,   true,   false,  false),
    (, 'Возврат',           true,   true,   false,  false),
;

-- Назначение: Описывает категорию ?!!
INSERT INTO storage.person_type (
    id, category_name)
    VALUES
    (, 'Категория-1')
;
