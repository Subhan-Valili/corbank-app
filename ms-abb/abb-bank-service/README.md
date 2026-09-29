# abb-bank-service

Отдельный микросервис, полностью закрывающий интеграцию с **ABB Business API
Integration Service (REST API v1.6)** — всеми 22 методами из документа. Своя
БД (**Postgres, в Docker**), свой порт (**8081**), свой Maven-модуль. PASHA
Bank сюда не входит — это отдельный будущий микросервис, здесь не трогали.

## Зачем своя БД

Не просто "для галочки" — два реальных назначения:

- **`payment_batch`** — локальная запись каждого отправленного платёжного файла
  (batchNumber, externalReference, тип, последний известный статус). Это даёт
  аудит-историю независимо от того, сколько ABB хранит данные у себя, и
  позволяет отвечать на вопрос "что мы вообще отправляли" без похода в банк.
- **`account_snapshot`** — последний известный баланс по каждому счёту.
  Обновляется при каждом успешном запросе `GET balance`. Если ABB временно
  недоступен, `AbbAccountService.getBalance()` отдаёт этот снепшот вместо
  ошибки (с пометкой `"source": "snapshot-fallback"`), чтобы дашборд не падал
  из-за временного сбоя у банка.

## Архитектура: гексагональная (порты/адаптеры) + немного DDD

```
src/main/java/az/corbank/abb/
├── domain/                          ЯДРО. Ноль зависимостей от Spring/JPA/Jackson.
│   ├── model/                        Value objects (records) + один настоящий агрегат:
│   │                                  PaymentBatch — у него есть identity (batchNumber) и
│   │                                  поведение (applyRemoteStatus() — единственный реальный
│   │                                  переход состояния в этом сервисе). Всё остальное —
│   │                                  плоские records: AccountBalance, AccountStatement,
│   │                                  StatementLine, CorporateAccount, справочники и т.д.
│   └── exception/                    AbbGatewayException, PaymentBatchNotFoundException
│
├── application/                     USE CASE'Ы. Оркестрируют домен через порты.
│   ├── port/
│   │   ├── in/                       10 интерфейсов use case'ов — SubmitPaymentUseCase,
│   │   │                              GetAccountBalanceUseCase и т.д. Их реализуют сервисы
│   │   │                              ниже; их вызывают контроллеры (adapter/in/web).
│   │   └── out/                      Что нужно домену от внешнего мира, на языке домена:
│   │                                  AbbBankGateway (весь контракт похода в ABB — ни одного
│   │                                  сырого JSON-поля ABB тут нет), PaymentBatchRepositoryPort,
│   │                                  AccountSnapshotRepositoryPort.
│   └── service/                      AccountApplicationService, PaymentApplicationService,
│                                      ReferenceDataApplicationService — ОБЫЧНЫЕ Java-классы,
│                                      без @Service. Не обязаны знать, что работают внутри
│                                      Spring. Их биндит config/UseCaseConfig.java.
│
└── adapter/                         ГРАНИЦА С ВНЕШНИМ МИРОМ. Знает и про домен, и про фреймворк.
    ├── in/web/                        Driving-адаптер: REST-контроллеры + web DTO + обработчик
    │                                  ошибок. Зависят только от портов in/, никогда — от
    │                                  application/service/ напрямую и никогда — от adapter/out/.
    └── out/
        ├── abbclient/                 Driven-адаптер к РЕАЛЬНОМУ ABB Bank: AbbHttpGateway
        │                              (реализует AbbBankGateway) + AbbTokenService + dto/
        │                              (сырые JSON-формы ABB). Единственное место во всём
        │                              сервисе, которое знает реальные имена полей ABB.
        └── persistence/                Driven-адаптер к Postgres: JPA-сущности + Spring Data
                                       репозитории + *PersistenceAdapter классы (реализуют
                                       PaymentBatchRepositoryPort/AccountSnapshotRepositoryPort,
                                       маппят JPA-сущность ↔ доменный объект).

config/                              Composition root: AbbProperties, RestClientConfig,
                                      UseCaseConfig (@Bean-фабрики для application-сервисов).
```

**Правило зависимостей** (кто на кого может импортировать):
`adapter` → `application` → `domain`, и `adapter/in` никогда не видит `adapter/out` напрямую
— только через порты. Обратных стрелок нет: `domain` не знает о существовании Spring,
`application` не знает о существовании HTTP или Postgres.

**Что это даёт на практике:** захотим заменить Postgres на Mongo — трогаем только
`adapter/out/persistence/`. Захотим замокать ABB для тестов — подсовываем свою реализацию
`AbbBankGateway`, не трогая ничего выше порта. Смена всей схемы JSON-ответов ABB не выйдет
за пределы `adapter/out/abbclient/`.

## Внутренний REST API (для Feign-клиента из corbank-backend)

| Метод | Путь | Соответствует спеке ABB |
|---|---|---|
| GET | `/internal/abb/accounts/{accountNumber}/balance` | §4.9 |
| GET | `/internal/abb/accounts/{accountNumber}/statement?fromDate=&toDate=&page=&pageSize=&operationType=` | §4.10 |
| POST | `/internal/abb/payments` (`type`: REGULAR/SIGNED/OTP/SALARY/SALARY_SIGNED) | §4.2-4.4, 4.11-4.12 |
| POST | `/internal/abb/payments/verify-otp` | §4.5 |
| GET | `/internal/abb/payments/{batchNumber}` | §4.7 / §4.13 |
| GET | `/internal/abb/payments/{batchNumber}/{paymentId}` | §4.8 |
| GET | `/internal/abb/payments/file-status?externalReference=` | §4.6 |
| GET | `/internal/abb/payments` | локальная история батчей (без похода в ABB) |
| GET | `/internal/abb/budget-types` `/budget-codes` `/bank-codes` `/foreign-bank-codes` `/currency-rates` | §4.14-4.18 |
| GET | `/internal/abb/accounts-by-cif` | §4.21 |
| GET | `/internal/abb/swift/track?referenceId=` | §4.20 |
| POST | `/internal/abb/swift/files` (multipart) | §4.19 |
| GET | `/internal/abb/debit-advice?rrn=` (отдаёт PDF) | §4.22 |

## Важные оговорки по спеке

- **§4.8** (`GET /payments/{batchNumber}/paymentId`) — в документе URL показывает
  `paymentId` как часть пути, а таблица параметров классифицирует его как **query**.
  Я реализовал как path-переменную (`/payments/{batchNumber}/{paymentId}`), раз URL
  показывает именно так. Если песочница ABB вернёт 404 — поменяй на query-параметр
  в `AbbBankClient.getIndividualPayment()`.
- **§4.21** (`GET /payments/corporate-account-info`) — JSON-пример в самой спеке
  синтаксически битый (перепутаны фигурные скобки, задвоены поля). `AbbCorporateAccountDto`
  сделан как объединение всех полей, встретившихся в примере, все опциональны.
- 5 reference-data методов (**§4.14-4.18**: budget-type/code, bank/param,
  foreign-bank/param, currency-rate) в спеке показаны **без заголовка Authorization** —
  так и реализовано (`AbbBankClient.unauthenticated(...)`). Если в реальной
  песочнице это не так — легко добавить `withAuth` вместо `unauthenticated`.

## База данных — Postgres в Docker

По умолчанию сервис ждёт Postgres (не H2) — под это и заточен `docker-compose.yml`.

## Запуск (основной способ — Docker)

```bash
docker compose up --build
```

Поднимет два контейнера:
- **`db`** — Postgres 16, данные хранятся в volume `abb-bank-service-db-data`
  (переживают `docker compose down`, стираются только через `docker compose down -v`)
- **`abb-bank-service`** — сам сервис, ждёт `db` через healthcheck (`pg_isready`)
  прежде чем стартовать, поднимается на `http://localhost:8081`

Реальных данных для ABB (`ABB_API_USERNAME`/`ABB_API_PASSWORD`) пока нет — это
нормально, сервис поднимется и БД будет работать; сами вызовы к ABB Bank
вернут 502 до тех пор, пока не подключим настоящий демо-аккаунт (это
следующий шаг, отдельно от Docker/БД).

Если понадобится переопределить креды БД или (когда будут) реальные ABB-креды:
```bash
cp .env.example .env
# отредактируй .env
docker compose up --build
```

Пересборка после изменения кода:
```bash
docker compose up --build abb-bank-service
```

Смотреть логи:
```bash
docker compose logs -f abb-bank-service
```

Зайти в БД напрямую (для отладки):
```bash
docker compose exec db psql -U abb -d abb_bank_service
```

Остановить всё:
```bash
docker compose down          # контейнеры вниз, данные БД остаются
docker compose down -v       # + стереть данные БД
```

## Запуск без Docker (быстрая проверка, без Postgres)

Профиль `h2` поднимает сервис на in-memory H2 вместо Postgres — данные не
переживают рестарт, зато не нужен ни Docker, ни установленный Postgres:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```
Веб-консоль H2 (для отладки) — `http://localhost:8081/h2-console`.

## Конфигурация ABB (когда появятся реальные креды)

```bash
ABB_API_BASE_URL=https://api-test-c2b.abb-bank.az   # или прод-хост, когда получите
ABB_API_USERNAME=...
ABB_API_PASSWORD=...
```
При запуске через `docker compose` — прописать в `.env` (см. `.env.example`).
При запуске напрямую — экспортировать как переменные окружения перед `mvn spring-boot:run`.

## ⚠️ Компиляция/сборка не проверялась

Как и с прошлыми Java-проектами — здесь нет доступа к Maven Central/Docker Hub,
чтобы прогнать `mvn compile` или `docker compose build` в этой среде.
Структура, импорты и Dockerfile выверены вручную, но стоит прогнать сборку
у себя перед тем как полагаться на неё — особенно первый `docker compose up --build`,
который качает базовые образы и все Maven-зависимости с нуля.
