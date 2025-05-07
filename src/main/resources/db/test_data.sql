
SET SEARCH_PATH = finance_monitoring;
-- ------------------------------------------------------------------

COPY categories (name) FROM stdin;
Покупка
\.

COPY person_types (name) FROM stdin;
Физическое лицо
Юридическое лицо
\.

COPY transaction_statuses (name) FROM stdin;
Новая
Подтвержденная
В обработке
Отменена
Платеж выполнен
Платеж удален
Возврат
\.

COPY transaction_types (name) FROM stdin;
Поступление
Списание
\.
