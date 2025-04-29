COPY public.categories (id, name) FROM stdin;
1	Покупка
\.

COPY public.person_types (id, name) FROM stdin;
0	Физическое лицо
1	Юридическое лицо
\.

COPY public.transaction_statuses (id, name) FROM stdin;
1	Новая
2	Подтвержденная
3	В обработке
4	Отменена
5	Платеж выполнен
6	Платеж удален
7	Возврат
\.

COPY public.transaction_types (id, name) FROM stdin;
1	Поступление
2	Списание
\.
