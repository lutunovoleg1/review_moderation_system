# Stage 1: Сборка Java приложения
FROM gradle:8.11-jdk21-alpine AS java-builder

WORKDIR /app

# Копируем gradle конфигурацию
COPY review-moderation-system/build.gradle.kts review-moderation-system/settings.gradle.kts ./
COPY review-moderation-system/gradle ./gradle
COPY review-moderation-system/gradlew ./

# Загружаем зависимости (для кеширования слоев)
RUN ./gradlew dependencies --no-daemon || true

# Копируем исходники
COPY review-moderation-system/src ./src

# Собираем приложение
RUN ./gradlew bootJar --no-daemon

# Stage 2: Подготовка Python окружения
FROM python:3.10-slim AS python-builder

WORKDIR /app

# Устанавливаем зависимости для сборки Python пакетов
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    g++ \
    && rm -rf /var/lib/apt/lists/*

# Копируем requirements и устанавливаем зависимости
COPY requirements.txt .
RUN pip install --no-cache-dir --user -r requirements.txt

# Stage 3: Final runtime образ
FROM python:3.10-slim

WORKDIR /app

# Устанавливаем Java JRE 21 из официального репозитория Eclipse Temurin
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    wget \
    ca-certificates \
    && mkdir -p /etc/apt/keyrings && \
    wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | tee /etc/apt/keyrings/adoptium.asc && \
    echo "deb [signed-by=/etc/apt/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list && \
    apt-get update && \
    apt-get install -y --no-install-recommends temurin-21-jre && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/temurin-21-jre-amd64
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Создаем non-root пользователя для безопасности
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Копируем Python зависимости из builder stage в домашнюю директорию appuser
COPY --from=python-builder --chown=appuser:appgroup /root/.local /home/appuser/.local

# Убеждаемся что Python пакеты в PATH
ENV PATH=/home/appuser/.local/bin:$PATH
ENV PYTHONPATH=/app:$PYTHONPATH

# Копируем JAR файл из builder stage
COPY --chown=appuser:appgroup --from=java-builder /app/build/libs/*.jar app.jar

# Копируем Python модель и скрипт
COPY --chown=appuser:appgroup run_model.py .
COPY --chown=appuser:appgroup prediction_pipeline ./prediction_pipeline

USER appuser

# Настройка переменных окружения для приложения
ENV PYTHON_SCRIPT_PATH=run_model.py
ENV PYTHON_WORKING_DIR=/app
ENV PYTHON_VENV_PATH=python3
ENV PYTHON_VENV_ACTIVATE=false

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Открываем порт
EXPOSE 8080

# Запуск приложения
ENTRYPOINT ["java", "-Xmx512m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-jar", "app.jar"]