# Supplier Feed Integration Service

A Spring Boot service that imports product feeds from suppliers in different formats, maps their fields to a single internal product model, and exposes the data through a REST API.

Each supplier sends data in its own shape:

| Internal field | Supplier A (CSV) | Supplier B (XML) |
|----------------|------------------|------------------|
| sku            | `item_no`        | `<sku>`          |
| name           | `desc`           | `<productName>`  |
| quantity       | `qty`            | `<stock>`        |
| price          | `unit_price`     | `<cost>`         |

Valid rows are created or updated by SKU. Invalid rows are rejected with a row-level error, and the rest of the feed is still imported.

## Tech stack

- Java 21, Spring Boot 3, Spring Data JPA
- PostgreSQL
- JUnit 5, Mockito, MockMvc
- Docker Compose, GitHub Actions
- OpenAPI (Swagger UI)

## Design

- `FeedMapper` interface with one implementation per format (`CsvFeedMapper`, `XmlFeedMapper`). `FeedMapperRegistry` picks the mapper at runtime, so adding a new supplier format means adding one class.
- `FeedImportService` handles the upsert inside a single transaction.
- `GlobalExceptionHandler` returns RFC 7807 problem details for bad input and missing products.
- The XML parser disables DOCTYPE declarations to block XXE attacks.

## Run

```bash
docker compose up -d --build
```

- App: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

Sample feeds are in `samples/`.

## API

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/feeds/{supplier}?format=CSV\|XML` | Import a feed file (multipart field `file`) |
| GET | `/api/products?supplier=` | List products, optionally by supplier |
| GET | `/api/products/{sku}` | Get one product |
| GET | `/api/products/low-stock?threshold=10` | Products below a stock threshold |

## Tests

```bash
mvn test
```
