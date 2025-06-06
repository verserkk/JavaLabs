# **AnimeService API Documentation**  

REST API для управления аниме, пользователями и их коллекциями.  

---

## **📌 Основные сущности**  
- **`User`** – пользователь (без пароля, только идентификация).  
- **`Anime`** – аниме-тайтл с метаданными.  
- **`Collection`** – подборка аниме, созданная пользователем.  
- **`Log`** – системные логи (создание, скачивание).  
- **`Visit`** – счётчик посещений.  

---

## **🔗 Endpoints**  

### **🎌 Аниме**  
| Метод | Путь | Описание |
|-------|------|----------|
| `GET` | `/api/animes` | Список всех аниме |
| `POST` | `/api/animes` | Добавить новое аниме |
| `POST` | `/api/animes/bulk` | Массовое добавление аниме |
| `GET` | `/api/animes/search` | Поиск аниме (по названию, жанру и т.д.) |
| `GET` | `/api/animes/{id}` | Получить аниме по ID |
| `PUT` | `/api/animes/{id}` | Обновить аниме |
| `DELETE` | `/api/animes/{id}` | Удалить аниме |

### **👥 Пользователи**  
| Метод | Путь | Описание |
|-------|------|----------|
| `GET` | `/api/users` | Список всех пользователей |
| `POST` | `/api/users` | Создать пользователя |
| `GET` | `/api/users/full` | Полная информация о пользователях (с коллекциями) |
| `GET` | `/api/users/search` | Поиск пользователей |
| `GET` | `/api/users/{id}` | Получить пользователя по ID |
| `PUT` | `/api/users/{id}` | Обновить пользователя |
| `DELETE` | `/api/users/{id}` | Удалить пользователя |
| `GET` | `/api/users/{id}/full` | Полная информация о пользователе (с коллекциями) |

### **📚 Коллекции**  
| Метод | Путь | Описание |
|-------|------|----------|
| `GET` | `/api/collections` | Список всех коллекций |
| `POST` | `/api/collections` | Создать коллекцию |
| `POST` | `/api/collections/bulk` | Массовое создание коллекций |
| `GET` | `/api/collections/search` | Поиск коллекций |
| `GET` | `/api/collections/search/anime` | Поиск коллекций по аниме |
| `GET` | `/api/collections/user/{userId}` | Коллекции пользователя |
| `GET` | `/api/collections/{id}` | Получить коллекцию по ID |
| `PUT` | `/api/collections/{id}` | Обновить коллекцию |
| `DELETE` | `/api/collections/{id}` | Удалить коллекцию |

### **📜 Логи**  
| Метод | Путь | Описание |
|-------|------|----------|
| `GET` | `/api/logs/create` | Запустить создание лог-файла (асинхронно) |
| `GET` | `/api/logs/download` | Скачать лог-файл |
| `GET` | `/api/logs/{logId}/status` | Проверить статус создания лога |

### **📊 Посещения**  
| Метод | Путь | Описание |
|-------|------|----------|
| `GET` | `/api/visits/count` | Получить счётчик посещений |
| `GET` | `/api/visits/result` | Получить статистику посещений (JSON) |

---

## **📌 Примеры запросов**  

### **1. Создание пользователя**  
```http
POST /api/users
Content-Type: application/json

{
  "name": "AnimeLover",
  "email": "anime@example.com"
}
```

### **2. Добавление аниме в коллекцию**  
```http
POST /api/collections
Content-Type: application/json

{
  "userId": 1,
  "animeIds": [101, 205, 307],
  "title": "Лучшие аниме 2024"
}
```

### **3. Поиск аниме**  
```http
GET /api/animes/search?genre=shounen&year=2023
```

### **4. Получение логов**  
```http
GET /api/logs/create?date=2024-05-20
```
→ Возвращает `logId`, затем можно проверить статус:  
```http
GET /api/logs/abc123-xyz/status?date=2024-05-20
```

---

## **⚙️ Технологии**  
- **Backend**: Java (Spring Boot)  
- **Документация**: Swagger (OpenAPI)  
- **Логирование**: Logback + ELK (опционально)  
- **Тестирование**: JMeter (нагрузочное), Selenium (UI)  

---

## **🚀 Запуск**  
```bash
mvn spring-boot:run
```
Документация API: `http://localhost:8080/swagger-ui.html`  

---

### **📌 Особенности**  
✅ **Без паролей** – пользователи идентифицируются по ID/email.  
✅ **Асинхронные логи** – запрос на создание → проверка статуса → скачивание.  
✅ **Поиск по аниме/коллекциям** – фильтрация по жанру, году и т.д.  

---

**🎯 Цель проекта**: Упрощённая система для фанатов аниме с возможностью создания персональных коллекций.

[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=splitmindq_calorie-counter)](https://sonarcloud.io/summary/new_code?id=splitmindq_calorie-counter)
