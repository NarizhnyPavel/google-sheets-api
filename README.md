# Сервис по работе с google sheets api

**Применение**: для публикации данных (к примеру различных отчётов) в электронные таблицы.

**Преимущества**:
* прозрачное использование google api 
* быстрое внедрение
* простота настройки общего доступа к документам  
* легковесное решение для публикации данных в автоматизированных системах 

## Описание api
[Open-api (Swagger UI)](https://gooogle-sheets-api.herokuapp.com/swagger-ui.html)

**/api/sheets**

Обязательные поля: `"tableName", "sheetName", "values"`

Опциональные поля: 
* **["conditionalRules"](https://developers.google.com/sheets/api/guides/conditional-format?hl=ru) и ["sheetFormat"](https://developers.google.com/sheets/api/reference/rest/v4/spreadsheets/cells?hl=ru#cellformat)** содержат batchUpdate запросы.
При этом conditionalRules применяются только при создании нового листа таблицы
* **"range"** содержит ячейку начала диапазона записи. К примеру: "B2"
* **"permissions"** содержит настройки прав доступа к таблице. Обновляет права доступа, не удаляя остальные

При отправке запроса приложение:
1. создаёт или проверяет наличие в БД данных об указанной таблице и листе.
2. применяет необходимые форматирования листа
3. вычисляет диапазон записи на основании преданных значений
4. записывает значения в таблицу и возвращает url на лист таблицы, куда были записаны значения

**/api/drive**

`POST /api/drive/{fileId}` обновляет права доступа к файлу с удалением остальных прав

`PATCH /api/drive/{fileId}` обновляет права доступа к файлу **без** удаления остальных прав



### Авторизация
В приложении используется OAuth2 авторизация для доступа к googleApi. Для работы приложения необходимо получить refreshToken. Для этого:
1. Получить AuthCode предоставив разрешения приложению по [ссылке](https://gooogle-sheets-api.herokuapp.com/token/auth)
2. Получить refreshToken по запросу `/token/refresh` (необходим полученный AuthCode)

### Профили запуска

**dev** используется для локального запуска с созданием h2 file base

**api** для запуска приложения на heroku сервере

**prom** использует подключение к postgres БД 

### Протестировать приложение 
1. Получить AuthCode предоставив разрешения приложению по [ссылке](https://gooogle-sheets-api.herokuapp.com/token/auth)
2. Получить jwt token по запросу `/token/auth` (необходим полученный AuthCode)
2. Добавить полученный token в заголовок запросов с префиксом Bearer(Блок **Authorize** справа наверху swagger-ui)
3. Отправить запрос на `/api/sheets`

### Сборка приложения
0. удалить директорию `./auth` содержит авторизацию для профиля **api**, поэтому _опционально_
1. получить authCode/refreshToken по инструкции выше, вставить в [environment](./docker-compose.yml) в одно из соответствующих полей   
2. собираем jar
```shell script
# загружает gradle wrapper
./gradlew wrapper

# сборка проекта, прогон unit-тестов
./gradlew clean build 
```
3. запуск docker-compose 
```shell script
docker-compose up -d
```
