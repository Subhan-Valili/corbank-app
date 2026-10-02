# abb-bank-service

Микросервис под текущую задачу — **работающая `Əsas səhifə`**: список счетов
+ история операций (кто платил, сколько, когда). Из 22 методов ABB Business
API v1.6 задействовано ровно **2**: §4.21 (список счетов) и §4.10 (выписка).
Остальные 20 (платежи, OTP, справочники, свифт, дебетовое авизо) я реализовывал
раньше во весь охват спеки, но убрал — ими ничего в UI не пользовалось, а
мёртвый код только усложняет ревью и синхронизацию со спекой без всякой пользы.
Возвращаются тем же способом (доменная модель + метод порта + реализация в
гейтвее + контроллер), когда какая-то из них реально понадобится экрану.

Своя БД (**Postgres, в Docker**), порт **8083**. PASHA Bank сюда не входит.

## Зачем своя БД

Ровно одно назначение — **история операций**, а не аудит платежей (это была
предыдущая, неверная формулировка):

- **`operation_history`** — каждая строка выписки, полученная от ABB, оседает
  здесь (upsert по `accountNumber + reference`, так что повторный запрос
  пересекающегося периода не плодит дубли). Дальше "кто платил, сколько и
  когда" читается **прямо из БД**, без похода к ABB — это то, что кормит
  `Son əməliyyatlar` на дашборде на каждой загрузке страницы, не дёргая банк
  каждый раз.
- Данные в истории появляются только как побочный эффект вызова `GET .../statement`
  (§4.10) — то есть первый раз кто-то должен реально открыть выписку по счёту,
  прежде чем история будет что показать. Если ничего ещё не запрашивали —
  `history/recent` вернёт пустой список, это не ошибка.

## Архитектура: гексагональная (порты/адаптеры) + немного DDD

```
src/main/java/az/corbank/abb/
├── domain/                 ЯДРО. Ноль зависимостей от Spring/JPA/Jackson.
│   ├── model/                CorporateAccount, AccountStatement, StatementLine,
│   │                          StatementQuery, Direction — плоские value objects.
│   └── exception/             AbbGatewayException
│
├── application/            USE CASE'Ы. Оркестрируют домен через порты.
│   ├── port/
│   │   ├── in/                ListAccountsUseCase, GetAccountStatementUseCase,
│   │   │                       GetOperationHistoryUseCase — их вызывают контроллеры.
│   │   └── out/                AbbBankGateway (контракт похода в ABB, на языке
│   │                            домена — ни одного сырого JSON-поля ABB тут нет),
│   │                            OperationHistoryRepositoryPort (контракт БД).
│   └── service/                AccountApplicationService — ОБЫЧНЫЙ Java-класс,
│                                без @Service. Пишет историю как побочный эффект
│                                getStatement(), читает её отдельным use case'ом.
│                                Биндит его config/UseCaseConfig.java.
│
└── adapter/                ГРАНИЦА С ВНЕШНИМ МИРОМ.
    ├── in/web/                AccountController + web DTO + обработчик ошибок.
    │                          Знает только про порты in/, никогда — про adapter/out/
    │                          напрямую.
    └── out/
        ├── abbclient/          AbbHttpGateway (реализует AbbBankGateway) +
        │                       AbbTokenService + dto/ (сырые JSON-формы ABB).
        │                       Единственное место, которое знает реальные поля ABB.
        └── persistence/         JPA-сущность OperationHistoryJpaEntity + Spring Data
                                 репозиторий + PersistenceAdapter (реализует
                                 OperationHistoryRepositoryPort, маппит JPA ↔ домен,
                                 парсит `dd.MM.yyyy` от ABB в настоящий LocalDate для
                                 корректной сортировки "последних" операций).

config/                    Composition root: AbbProperties, RestClientConfig, UseCaseConfig.
```

**Правило зависимостей:** `adapter → application → domain`, `adapter/in` не видит
`adapter/out` напрямую — только через порты.

## Внутренний REST API (для Feign-клиента из corbank-backend)

| Метод | Путь | Что это |
|---|---|---|
| GET | `/internal/abb/accounts` | список счетов — спека §4.21 |
| GET | `/internal/abb/accounts/{accountNumber}/statement?fromDate=&toDate=&page=&pageSize=&operationType=` | ЖИВОЙ вызов к ABB (§4.10); пишет результат в историю как побочный эффект |
| GET | `/internal/abb/accounts/{accountNumber}/history?limit=` | история одного счёта — чтение из БД, к ABB не ходит |
| GET | `/internal/abb/accounts/history/recent?limit=` | история по всем счетам — то, что кормит `Son əməliyyatlar` |

## Важные оговорки по спеке (актуальны для обоих задействованных методов)

- **§4.21** (`GET /payments/corporate-account-info`) — JSON-пример в самой спеке
  синтаксически битый (перепутаны фигурные скобки, задвоены поля). `AbbCorporateAccountDto`
  сделан как объединение всех полей, встретившихся в примере, все опциональны.
- Метод-строка спеки для §4.21 (`/payments/corporate-account-info`, с дефисом)
  и её собственная URL-строка (`.../corporateaccount-info`, без дефиса)
  противоречат друг другу — использован вариант с дефисом. Стоит перепроверить
  на реальном стенде.

## Mock-режим (пока нет реального доступа к ABB)

`abb.api.mock-enabled` (по умолчанию — **`true`** прямо сейчас, см. `application.yml`)
переключает между двумя реализациями `AbbBankGateway`:

- **`AbbHttpGateway`** — реальные вызовы к ABB. Активна при `mock-enabled=false`.
- **`AbbMockGateway`** — активна при `mock-enabled=true`. Никаких хардкод-данных в Java
  после первого запуска: при пустых таблицах `mock_account`/`mock_operation` она один раз
  засеивает их демо-счетами и 4 демо-операциями, а дальше **только читает из Postgres**.
  Можно зайти в базу и поправить/добавить строки руками — гейтвей отдаст то, что реально
  лежит в таблице, без пересборки кода.

Обе реализации взаимозаменяемы — выше порта `AbbBankGateway` ничего не меняется, включая
запись в `operation_history` (она происходит как побочный эффект `getStatement()` в
`AccountApplicationService`, независимо от того, откуда пришли данные — от реального ABB
или от мока).

Когда появится реальный сетевой доступ к ABB (whitelisting/VPN/сертификат) и креды —
просто:
```
ABB_API_MOCK_ENABLED=false
```
Ничего больше менять не нужно.

## База данных — Postgres в Docker

```bash
docker compose up --build
```

Поднимет `db` (Postgres 16, данные в volume `abb-bank-service-db-data`,
переживают `docker compose down`) и `abb-bank-service` (ждёт БД через healthcheck,
порт `8083`). Без реальных `ABB_API_USERNAME`/`ABB_API_PASSWORD` сервис и БД всё
равно поднимутся нормально — сами вызовы к ABB вернут 502, это ожидаемо до
подключения демо-аккаунта.

```bash
cp .env.example .env    # креды БД уже с рабочими дефолтами; сюда же — реальные ABB-креды
docker compose up --build
docker compose logs -f abb-bank-service
docker compose exec db psql -U abb -d abb_bank_service   # зайти в БД напрямую
docker compose down                                       # данные БД остаются
docker compose down -v                                     # + стереть данные БД
```

## Запуск без Docker (быстрая проверка, без Postgres)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```
БД — in-memory H2, данные не переживают рестарт. Веб-консоль — `http://localhost:8083/h2-console`.

## Конфигурация ABB (когда появятся реальные креды)

```
ABB_API_BASE_URL=https://api-test-c2b.abb-bank.az
ABB_API_USERNAME=...
ABB_API_PASSWORD=...
```
В `.env` при запуске через Docker; переменными окружения — при прямом `mvn spring-boot:run`.

## ⚠️ Компиляция/сборка не проверялась

Нет доступа к Maven Central/Docker Hub в этой среде, чтобы прогнать `mvn compile`
или `docker compose build` здесь. Прогони сборку у себя перед тем как полагаться
на неё.
