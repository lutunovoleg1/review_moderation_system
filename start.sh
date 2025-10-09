#!/bin/bash

# Настройки
IMAGE_NAME="review-moderation-system"
CONTAINER_NAME="review-moderation-app"
PORT=8080

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== Review Moderation System - Docker Manager ===${NC}\n"

# Проверяем запущен ли уже контейнер
if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo -e "${YELLOW}Контейнер ${CONTAINER_NAME} уже существует${NC}"
    
    if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        echo -e "${GREEN}Контейнер уже запущен${NC}"
        echo -e "\nДоступные команды:"
        echo -e "  docker logs -f ${CONTAINER_NAME}  - просмотр логов"
        echo -e "  docker stop ${CONTAINER_NAME}     - остановка"
        echo -e "  docker restart ${CONTAINER_NAME}  - перезапуск"
        exit 0
    else
        echo -e "${YELLOW}Запускаем существующий контейнер...${NC}"
        docker start ${CONTAINER_NAME}
        echo -e "${GREEN}Контейнер запущен${NC}"
        echo -e "\nПриложение доступно на: http://localhost:${PORT}"
        echo -e "Логи: docker logs -f ${CONTAINER_NAME}"
        exit 0
    fi
fi

# Проверяем существует ли образ
if ! docker images --format '{{.Repository}}:{{.Tag}}' | grep -q "^${IMAGE_NAME}:latest$"; then
    echo -e "${YELLOW}Образ не найден. Начинаем сборку...${NC}\n"
    
    # Сборка образа
    echo -e "${GREEN}Сборка Docker образа...${NC}"
    if docker build -t ${IMAGE_NAME}:latest .; then
        echo -e "${GREEN}✓ Образ успешно собран${NC}\n"
    else
        echo -e "${RED}✗ Ошибка при сборке образа${NC}"
        exit 1
    fi
else
    echo -e "${GREEN}Образ ${IMAGE_NAME}:latest уже существует${NC}\n"
fi

# Запуск контейнера
echo -e "${GREEN}Запуск контейнера...${NC}"
if docker run -d \
    --name ${CONTAINER_NAME} \
    -p ${PORT}:8080 \
    --restart unless-stopped \
    ${IMAGE_NAME}:latest; then
    echo -e "${GREEN}✓ Контейнер успешно запущен${NC}\n"
else
    echo -e "${RED}✗ Ошибка при запуске контейнера${NC}"
    exit 1
fi

# Ждем запуска приложения
echo -e "${YELLOW}Ожидание запуска приложения...${NC}"
sleep 5

# Проверяем здоровье контейнера
echo -e "\n${GREEN}Статус контейнера:${NC}"
docker ps --filter "name=${CONTAINER_NAME}" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo -e "\n${GREEN}=== Готово ===${NC}"
echo -e "Приложение доступно на: ${GREEN}http://localhost:${PORT}${NC}"
echo -e "Actuator health: ${GREEN}http://localhost:${PORT}/actuator/health${NC}"
echo -e "\nПолезные команды:"
echo -e "  ${YELLOW}docker logs -f ${CONTAINER_NAME}${NC}     - просмотр логов в реальном времени"
echo -e "  ${YELLOW}docker stop ${CONTAINER_NAME}${NC}        - остановка контейнера"
echo -e "  ${YELLOW}docker restart ${CONTAINER_NAME}${NC}     - перезапуск контейнера"
echo -e "  ${YELLOW}docker rm -f ${CONTAINER_NAME}${NC}       - удаление контейнера"
echo -e "  ${YELLOW}docker rmi ${IMAGE_NAME}:latest${NC}  - удаление образа"
echo -e "\nПример запроса к API:"
echo -e "  ${YELLOW}curl -X POST http://localhost:${PORT}/api/v1/reviews/moderate \\${NC}"
echo -e "  ${YELLOW}  -H 'Content-Type: application/json' \\${NC}"
echo -e "  ${YELLOW}  -d '{\"review\": \"Отличный продукт!\"}'${NC}"
