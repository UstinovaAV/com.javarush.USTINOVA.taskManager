# Task Manager API (1 спринт)

Финальный проект 5 модуля. RESTful API для управления задачами пользователей.

## Технологический стек
- **Java 17**
- **Spring Boot 4.1.1** (Web, Data JPA, Validation)
- **PostgreSQL** 
- **Liquibase** (Миграции схемы БД)
- **Docker & Docker Compose** 
- **Lombok** 

## Как запустить

Для запуска требуется установленный **Docker** и **Java 17+** (для сборки).

1. Склонируйте репозиторий:
   ```bash
   git clone 
   cd taskManager
   ```
2. Соберите проект (создаст JAR-файл в папке target):
```
mvn clean package -DskipTests
```
3. Запустите приложение и базу данных командой
```bash
docker-compose up --build
```

При первом запуске Liquibase автоматически создаст таблицы, а DataInitializer заполнит базу тестовыми данными.

## Тестовые данные
При первом запуске в базе создаются:
Пользователь 1 (Admin): id = 1, username: Юрий
Пользователь 2 (User): id = 2, username: Анжелика Устинова и несколько тестовых задач, привязанных к этим пользователям.

## API Endpoints
Все запросы можно выполнять через Postman
GET запросы можно выполнять через браузер.

1. Получить все задачи пользователя
   + Метод: GET
   + URL 1: http://localhost:8080/api/tasks?userId=1
   + URL 2: http://localhost:8080/api/tasks?userId=1

   Описание: Возвращает список задач для указанного userId.
2. Создать новую задачу
   + Метод: POST
   + URL: http://localhost:8080/api/tasks?userId=1
   + Headers: Content-Type: application/json 
   + Body:
   ```json
   {
      "title": "Сделать домашнее задание",
      "description": "Завершить финальный проект",
      "deadline": "2026-09-21"
   }
   ```
3. Изменить статус задачи
   + Метод: PUT
   + URL: http://localhost:8080/api/tasks/1/status?status=IN_PROGRESS
   + Доступные статусы: TODO, IN_PROGRESS, DONE.
4. Удалить задачу
   + Метод: DELETE 
   + URL: http://localhost:8080/api/tasks/1
   + Полное удаление задачи из базы данных. (в дальнейшем будет реализован SOFT DELETE)
     
На текущем этапе (MVP) аутентификация через JWT/Spring Security отключена для упрощения тестирования API. userId передается как параметр запроса. Модуль безопасности будет добавлен в следующем спринте.