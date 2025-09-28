# Review Moderation System
Review Moderation System is an educational project that illustrates the architecture of an information system for automatic moderation of textual reviews. The system processes user-generated content, classifies it into categories such as acceptable, spam, or offensive, and produces a structured report.

## Prediction Pipeline

Система использует пайплайн предсказания, основанный на CatBoost классификаторе с эвристическими признаками.

### Архитектура

```
Input Text → Feature Extraction → CatBoost Model → Prediction
```

### Компоненты

#### 1. `Model` (model.py)
Основной класс для инференса модели:
- Загружает предобученную CatBoost модель (.cbm)
- Принимает на вход список строк или pandas DataFrame
- Возвращает предсказания классов или детальную информацию с вероятностями

```python
from model import Model

model = Model()

# Простые предсказания (только классы)
predictions = model.predict(['Отличный фильм!', 'Плохой сервис'])

# Детальные предсказания (класс + вероятность)
detailed = model.predict_detailed(['Отличный фильм!'])
```

#### 2. `Features` (features.py)
Класс для извлечения эвристических признаков:
- **Базовые метрики**: длина текста, средняя длина слов
- **Пунктуация**: отношение знаков препинания, восклицательных и вопросительных знаков
- **Капитализация**: отношение слов и символов в верхнем регистре
- **Лексические признаки**: стоп-слова, уникальные слова, позитивные/негативные слова
- **Эмоциональные маркеры**: усилители, ослабители, нецензурная лексика

#### 3. Словари (dicts.py)
Содержит предопределенные списки слов для анализа:
- `positive_words` - позитивная лексика
- `negative_words` - негативная лексика
- `intensifiers` - усилители эмоций
- `diminishers` - ослабители эмоций
- `bad_words` - нецензурная лексика
- `stop_words` - стоп-слова

### Классы предсказаний

- **0**: NEUTRAL - нейтральный тон
- **1**: POSITIVE - позитивный тон  
- **2**: NEGATIVE - негативный тон

### Установка и настройка

#### 1. Создание виртуального окружения

```bash
# Создание виртуального окружения
python -m venv python-env

# Активация (Linux/macOS)
source python-env/bin/activate

# Активация (Windows)
python-env\Scripts\activate
```

#### 2. Установка зависимостей

```bash
pip install -r requirements.txt
```

### Использование

```python
from prediction_pipeline.model import Model

# Инициализация модели
model = Model()

# Предсказание для одного текста
result = model.predict(['Этот товар просто супер!'])
print(result)  # [1] - POSITIVE

# Детальная информация
detailed = model.predict_detailed(['Этот товар просто супер!'])
print(detailed)  # [{'label': 1, 'probability': 0.85}]
```

### Требования

- Python 3.8+
- `catboost==1.2.8`
- `pandas==2.3.2`
- `pymorphy2==0.9.1`
- `numpy==2.0.2`
