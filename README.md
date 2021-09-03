# Сервис по работе с google sheets api 

Использует [api](https://developers.google.com/sheets/api/guides/concepts?hl=ru) для Создания и Обновления электронных таблиц google.

Может быть использован для публикации данных (к примеру различных отчётов) в электронные таблицы с последующим предоставлением доступа к ним.

Преимущества:
* прозрачное использование google api 
* быстрое внедрение
* легковесное решение для публикации данных в автоматизированных системах 

## Описание работы приложения

Основной запрос **/api/sheets**
с телом:
```shell script
{
  "conditionalRules": [],
  "sheetFormat": [],
  "range": "",
  "sheetName": "",
  "tableName": "",
  "values": [
    [
      ""
    ]
  ]
}
```
Опциональные поля: 
* **["conditionalRules"](https://developers.google.com/sheets/api/guides/conditional-format?hl=ru) и ["sheetFormat"](https://developers.google.com/sheets/api/reference/rest/v4/spreadsheets/cells?hl=ru#cellformat)** содержат запросы
При этом conditionalRules применяются только при создании нового листа таблицы
* **"range"** содержит ячейку начала диапазона записи  

При отправке запроса приложение:
1. создаёт или проверяет наличие в БД данных об указанной таблице и листе.
2. применяет необходимые форматирования листа
3. вычисляет диапазон записи на основании преданных значений
4. записывает значения в таблицу и возвращает url на лист таблицы, куда были записаны значения

### Авторизация
Для использования google api используется OAuth2 авторизация, для которой требуются AuthCredentials (их возможно получить в [форме](./src/main/resources/static/template/OAuth%20token.html))

## Протестировать приложение -> [перейти](https://gooogle-sheets-api.herokuapp.com/swagger-ui.html)
1. регистрируемся с AuthCredentials по запросу `/register` для получения jwt token
2. добавляем полученный token в заголовок запросов (Блок **Authorize**)
3. Отправляем запрос на `/api/sheets` 

### Профили запуска

**dev** используется для локального запуска с созданием h2 file base

**api** для запуска приложения на heroku сервере

**prom** использует подключение к postgres БД 

### Сборка приложения
0. удаляем директорию `./auth` содержит авторизацию для профиля **api**, поэтому _опционально_

1. собираем jar
```shell script
# загружает gradle wrapper 6.8
./gradlew wrapper

# сборка проекта, прогон unit-тестов
./gradlew clean build 
```
4. запускаем docker-compose 
```shell script
docker-compose up -d
```
