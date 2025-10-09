# 🚀 Запуск в Docker

## Быстрый старт

```bash
./start.sh
```

Готово! Приложение запустится на http://localhost:8080

## Проверка

```bash
curl -X POST http://localhost:8080/api/v1/reviews/moderate \
  -H 'Content-Type: application/json' \
  -d '{"review": "Отличный продукт!"}'
```

## Основные команды

```bash
# Посмотреть логи
docker logs -f review-moderation-app

# Остановить
docker stop review-moderation-app

# Перезапустить
docker restart review-moderation-app

# Удалить контейнер
docker rm -f review-moderation-app

# Удалить образ
docker rmi review-moderation-system:latest
```

## Альтернативный запуск через Docker Compose

```bash
# Запуск
docker-compose up -d

# Логи
docker-compose logs -f

# Остановка
docker-compose down
```

## Что внутри

- **Размер образа:** ~1.1 GB
- **Java:** OpenJDK 21 JRE
- **Python:** 3.10 с ML моделью (CatBoost)
- **Безопасность:** запуск от non-root пользователя
- **Оптимизация:** multistage build, минимизация слоев

## Troubleshooting

**Порт занят?**
```bash
# Измени порт в start.sh на строке PORT=8080
# Или запусти вручную:
docker run -d --name review-moderation-app -p 8081:8080 review-moderation-system:latest
```

**Контейнер не запускается?**
```bash
# Смотри логи
docker logs review-moderation-app
```

**Пересобрать образ?**
```bash
docker rm -f review-moderation-app
docker rmi review-moderation-system:latest
./start.sh
```

---

Просто запусти `./start.sh` и всё работает! 🎉
