# REST API: Anime

## Описание

Этот проект представляет собой **простой REST API** на **Spring Boot**, который позволяет получать информацию об аниме. API поддерживает **Query Parameters** и **Path Parameters**, возвращая JSON-ответы.

## Используемые технологии

- **Java 23**
- **Spring Boot**
- **Maven**
- **REST API**



После запуска API будет доступен по адресу:

```
http://localhost:8080/api
```

## 📡 API Эндпоинты

### 🔹 1. Получение аниме по имени (Query Parameters)

- **Метод:** `GET`
- **URL:** `/api/anime?name={animeName}`
- **Пример запроса:**
  ```sh
  curl -X GET "http://localhost:8080/api/anime?name=Naruto"
  ```
- **Ответ (JSON):**
  ```json
  {
    "animeName": "Naruto"
  }
  ```

### 🔹 2. Получение аниме по ID (Path Parameters)

- **Метод:** `GET`
- **URL:** `/api/anime/{animeId}`
- **Пример запроса:**
  ```sh
  curl -X GET "http://localhost:8080/api/anime/101"
  ```
- **Ответ (JSON):**
  ```json
  {
    "animeId": 101
  }
  ```

---








[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=verserkk_JavaLabs2&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=verserkk_JavaLabs2)
