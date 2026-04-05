# Task Manager (Spring Boot)

REST API для управления задачами:
JWT-аутентификация, роли `USER` / `ADMIN`,
CRUD по задачам, фильтры, схема БД через **Flyway**,
основная СУБД: **PostgreSQL**.
Приложение собирается в **Docker image** и вместе с Postgres поднимается через **Docker Compose**.

## Требования

- **JDK 17**
- **Maven 3.9+**
- **Docker** и **Docker Compose** (для контейнеров и для интеграционных тестов на Testcontainers)

## Сборка

```bash
mvn clean package
```

Сборка **не обязательна** перед первым запуском из IntelliJ IDEA: достаточно открыть проект и нажать Run у `TaskManagerApplication`.

## Запуск из IntelliJ IDEA

В проекте подключён `**spring-boot-docker-compose`**: при старте приложения **сначала** поднимается контейнер **PostgreSQL** из `docker-compose.yml`, дождитесь healthcheck, затем стартует Spring (Flyway, API на **8080**).

Нужен запущенный **Docker Desktop** (или другой демон Docker, в зависимости от ОС). Остановка приложения в IDE обычно останавливает и контейнеры.

Отключить авто-compose и использовать свой Postgres на `localhost`:

```text
-Dspring.docker.compose.enabled=false
```

(в **VM options** конфигурации Run в IDEA или через переменную окружения `SPRING_DOCKER_COMPOSE_ENABLED=false`.)

## Запуск через Docker

Только база:

```bash
docker compose up -d
```

База и **backend** в контейнере:

```bash
docker compose --profile full up --build -d
```

Остановка:

```bash
docker compose down
```

или

```bash
docker compose --profile full down
```

## Локальный запуск без Docker Compose

1. Поднимите PostgreSQL с БД `task_manager`, пользователь/пароль как в `docker-compose.yml` (`postgres` / `postgres`), порт **5432**.
2. Запустите с отключённым compose: `-Dspring.docker.compose.enabled=false` или задайте `SPRING_DOCKER_COMPOSE_ENABLED=false`.
3. В системе должен быть установлен maven или используйте `./mvnw` вместо `mvn`. Выполните :

```bash
mvn clean package -DskipTests
```

1. Далее выполните:

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.docker.compose.enabled=false"
```

Для Windows powershell:

```bash
mvn spring-boot:run "-Dspring-boot.run.jvmArguments=-Dspring.docker.compose.enabled=false"
```

Или после `mvn package`:

```bash
java -Dspring.docker.compose.enabled=false -jar target/task-manager-0.0.1-SNAPSHOT.jar
```

Для Windows powershell:

```bash
java "-Dspring.docker.compose.enabled=false" -jar target/task-manager-0.0.1-SNAPSHOT.jar
```

## Swagger / OpenAPI (ручное тестирование API)

После запуска приложения (локально или в Docker):

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

В Swagger нажмите **Authorize**, введите `Bearer <ваш_jwt_токен>`.

## Готовые HTTP-запросы

В каталоге `[http/](http/)` лежат файлы для **IntelliJ HTTP Client**:

- `[http/auth.http](http/auth.http)` — регистрация и логин
- `[http/tasks.http](http/tasks.http)` — CRUD задач и выборки с фильтрами

