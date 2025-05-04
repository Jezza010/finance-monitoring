COPY public.categories (name) FROM stdin;
Покупка
\.

COPY public.person_types (name) FROM stdin;
Физическое лицо
Юридическое лицо
\.

COPY public.transaction_statuses (name) FROM stdin;
Новая
Подтвержденная
В обработке
Отменена
Платеж выполнен
Платеж удален
Возврат
\.

COPY public.transaction_types (name) FROM stdin;
Поступление
Списание
\.
