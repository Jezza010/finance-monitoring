# finance-monitoring
Веб-приложение для мониторинга финансовых показателей и генерации отчетов

---

## Запуск контейнера:

1. Запустить:
####  docker compose up --build -d
-  для Windows предварительно поставить и запустить Docker Desktop

2. Будет создана БД 'finance_db'
- будет инициализирована файлом 'schema.sql', который сейчас лежит в database/, а теперь:
#### src/main/resources/schema.sql
- там добавлен администратор БД 'admin' с паролем 'admin'
- будет добавлена в проект папка services/
- если нужно обнулить БД (или меняем схему), то удаляем папку services/ и перезапускаем контейнер

3. Будет запущен pgAdmin по адресу:  http://localhost:5050
-  создать сервер с произвольным именем, например, 'Finance monitoring'
-  указать хост 'pg_db' (как в файле compose.yaml)
-  указать порт 5432 (как в файле compose.yaml)
-  указать имя БД 'finance_db' (как в файле compose.yaml)
-  пользователь: 'admin' (как в файле resources/schema.sql)
-  пароль: 'admin' (как в файле resources/schema.sql)

4. Можно пользоваться!

 ---

## Запуск БД:

Создание бд - 
`sudo -u postgres psql < database/schema.sql`
