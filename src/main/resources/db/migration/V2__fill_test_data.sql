-- Тестовые пользователи и задачи.
-- Пароль у всех одинаковый: password

INSERT INTO users (username, email, password, role)
VALUES ('aleksandr_volkov', 'aleksandr@mail.ru',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'USER'),
       ('mariya_kozlova', 'mariya@mail.ru',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'USER'),
       ('igor_semenov', 'igor@mail.ru',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN');

-- В работе
INSERT INTO tasks (title, description, status, priority, author_id, assignee_id, created_at, updated_at)
SELECT 'Сверка квартальных документов',
       'Проверить бухгалтерские проводки за квартал',
       'IN_PROGRESS',
       'HIGH',
       a.id,
       m.id,
       TIMESTAMPTZ '2026-02-01 09:00:00+00',
       TIMESTAMPTZ '2026-02-03 14:30:00+00'
FROM users a,
     users m
WHERE a.username = 'aleksandr_volkov'
  AND m.username = 'mariya_kozlova';

-- Без исполнителя
INSERT INTO tasks (title, description, status, priority, author_id, assignee_id, created_at, updated_at)
SELECT 'Подготовка к совещанию',
       'Собрать материалы для планёрки',
       'TODO',
       'LOW',
       a.id,
       NULL,
       TIMESTAMPTZ '2026-02-10 11:15:00+00',
       TIMESTAMPTZ '2026-02-10 11:15:00+00'
FROM users a
WHERE a.username = 'aleksandr_volkov';

-- Выполнено
INSERT INTO tasks (title, description, status, priority, author_id, assignee_id, created_at, updated_at)
SELECT 'Подписание акта выполненных работ',
       'Передать документы в архив',
       'DONE',
       'MEDIUM',
       m.id,
       a.id,
       TIMESTAMPTZ '2026-01-20 16:00:00+00',
       TIMESTAMPTZ '2026-01-22 10:00:00+00'
FROM users a,
     users m
WHERE a.username = 'aleksandr_volkov'
  AND m.username = 'mariya_kozlova';
