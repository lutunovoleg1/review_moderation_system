# Развертывание Review Moderation System

## Требования
- Docker
- Docker Compose

## Запуск
```bash
docker-compose up --build
```

## Сервисы
- Review Moderation System: http://localhost:8080
- Prediction Pipeline: Доступен только внутри Docker-сети

## Особенности
- Prediction Pipeline изолирован и не имеет публичного доступа
- Связь между сервисами осуществляется через внутреннюю сеть

## Остановка
```bash
docker-compose down
```
