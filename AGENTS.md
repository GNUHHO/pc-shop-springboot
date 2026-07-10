# Project Instructions

## Project Goal

This repository is a graduation project for an AI-integrated
Dynamic Pricing and Supply Chain Management Platform.

The platform manages:

- Products and categories
- Suppliers
- Warehouses and inventory
- Purchase orders
- Customer orders and order items
- Payments
- Inventory transactions
- Demand forecasting
- Dynamic price recommendations
- Price approval and price history
- Low-stock alerts
- Reorder recommendations

## Target Architecture

- Java Spring Boot provides the main REST API and business logic.
- PostgreSQL stores transactional and historical data.
- Spring Data JPA and Hibernate provide database access.
- A Python FastAPI service provides machine-learning predictions.
- Spring Boot communicates with the AI service through REST APIs.
- Postman is used for API testing.
- DataGrip is used for PostgreSQL administration.
- IntelliJ IDEA is the main Java IDE.

The expected request flow is:

1. Client or Postman sends an HTTP request.
2. Controller receives and validates the request.
3. Service processes business logic.
4. Repository communicates with PostgreSQL.
5. The service maps the result to a response.
6. Controller returns an appropriate HTTP response.

The expected AI flow is:

1. Spring Boot collects historical sales and inventory data.
2. Spring Boot sends prediction input to the Python FastAPI service.
3. The AI service returns a demand forecast or price recommendation.
4. Spring Boot stores the prediction and model version.
5. An authorized employee reviews the recommendation.
6. The recommendation is approved or rejected.
7. Only an approved recommendation may update the product price.

## Important Project Paths

- Java source code: `src/main/java`
- Application configuration: `src/main/resources`
- Test source code: `src/test/java`
- Maven configuration: `pom.xml`
- PostgreSQL DDL files: `docs/database/public/*.sql`
- Postman files: `docs/api/`
- AI service: `ai-service/`, when it is created

Before modifying an entity, repository, service, or database-related code,
read the relevant SQL files in:

`docs/database/public/`

## Java Package Responsibilities

- `controller`: REST API endpoints
- `service`: business logic and transaction boundaries
- `repository`: PostgreSQL access through Spring Data JPA
- `entity`: JPA entity mappings
- `dto`: request and response objects
- `exception`: custom exceptions and global exception handling
- `mapper`: entity and DTO conversion
- `config`: application configuration
- `security`: authentication and authorization
- `integration.ai`: communication with the Python AI service

Do not place business logic directly inside controllers.

Controllers should:

- Receive HTTP requests
- Validate request input
- Call services
- Return appropriate HTTP status codes

Services should:

- Implement business rules
- Control transactions
- Coordinate repositories
- Throw meaningful application exceptions

Repositories should:

- Perform database access
- Use specific queries instead of loading an entire table
- Avoid using `findAll()` to locate one specific record

## Database Rules

- PostgreSQL is the database.
- Java entities must match the SQL definitions in `docs/database/public/`.
- Do not modify the database schema without explicit permission.
- Do not create migrations unless explicitly requested.
- Do not remove foreign keys to solve application errors.
- Do not change column types without explaining the impact.
- Use `BigDecimal` for prices, costs, totals, and monetary values.
- Never use `double` or `float` for monetary values.
- Monetary columns should define suitable precision and scale.
- Inventory quantity must never become negative.
- Order quantity must always be greater than zero.
- SKU values must be unique.
- Required database fields must be validated before persistence.
- Order creation and inventory deduction must run in one transaction.
- Database credentials must not be committed to Git.

When a new database change is required:

1. Explain why the schema must change.
2. Identify the affected tables and relationships.
3. Propose the SQL change.
4. Explain migration risks and compatibility.
5. Wait for approval before changing the schema.

## Product Rules

- A product must belong to a valid category.
- Product SKU must be unique.
- Base price and current price must not be negative.
- Inactive products must not be sold.
- Product APIs should use DTOs rather than exposing entities directly.
- Product price changes must be recorded in price history.

## Inventory Rules

- Inventory must be associated with a valid product.
- Inventory quantity must not become negative.
- Do not load all inventory records to find inventory for one product.
- Use repository queries such as `findByProductId(...)`.
- Inventory-changing operations must be transactional.
- Inventory imports, exports, and adjustments should be recorded as transactions.
- Concurrency must be considered when multiple orders update the same inventory.

## Order Rules

- Order quantity must be greater than zero.
- A product must exist and be active before it can be ordered.
- Inventory must be checked before an order is confirmed.
- Inventory deduction and order saving must be atomic.
- An order must not be marked completed before required payment and fulfilment steps.
- Order totals must be calculated using `BigDecimal`.
- Do not trust price or total values sent directly by the client.
- The server must calculate prices from trusted database values.
- Use request and response DTOs.
- Avoid circular JSON serialization between `Orders` and `OrderItem`.

## API Rules

- Use RESTful endpoint naming.
- Use appropriate HTTP methods.
- Use appropriate HTTP status codes.
- Return `201 Created` when a new resource is created.
- Return `200 OK` for successful reads and updates when appropriate.
- Return `204 No Content` for successful deletion when appropriate.
- Return `400 Bad Request` for invalid input.
- Return `404 Not Found` when a requested resource does not exist.
- Return `409 Conflict` for business conflicts when appropriate.
- Do not return generic `500 Internal Server Error` for known business errors.
- Validate request DTOs with Jakarta Validation.
- Keep Postman collections synchronized with controller endpoints.
- Do not expose passwords, internal tokens, or sensitive entity fields.

## Exception Handling Rules

- Use meaningful custom exceptions.
- Prefer centralized exception handling with `@RestControllerAdvice`.
- Do not use generic `RuntimeException` for every business error.
- Error responses should contain useful but non-sensitive information.
- Error responses should be consistent across APIs.

Suggested error response fields:

- `timestamp`
- `status`
- `error`
- `message`
- `path`

## AI Rules

- AI recommendations must not automatically modify product prices.
- A recommendation must be approved before it is applied.
- Store the model name and model version for every prediction.
- Store the prediction creation time.
- Store the forecast period or target date.
- Store enough input context to explain the prediction.
- Never use random values as real AI predictions.
- Clearly separate rule-based pricing from machine-learning pricing.
- Machine-learning output must be validated by Spring Boot.
- Recommended prices must respect configured minimum and maximum limits.
- AI service failures must not break normal order processing.
- The system must handle AI service timeout or unavailable responses.
- All monetary values must use `BigDecimal` in Java.
- Training code, prediction code, and API code should be separated.
- Model evaluation metrics must be recorded.
- Training data must not contain passwords or unnecessary personal data.

The initial AI development order should be:

1. Rule-based price recommendation
2. Historical sales data collection
3. Demand forecasting
4. Price optimization
5. Explanation and approval workflow

Do not implement advanced AI before the transactional data model is stable.

## Security Rules

- Never commit database passwords.
- Never commit access tokens, API keys, or secret keys.
- Do not print secrets in logs.
- Do not expose password fields in API responses.
- Do not hard-code credentials in Java or Python files.
- Use environment variables or local configuration for credentials.
- Do not modify authentication or authorization rules unless explicitly requested.

## Coding Rules

- Follow the existing package structure.
- Use constructor injection.
- Prefer `final` dependencies.
- Keep controllers small.
- Keep business logic in services.
- Use DTOs at API boundaries.
- Use clear method and variable names.
- Avoid duplicate code.
- Avoid unnecessary dependencies.
- Do not rename public endpoints without explicit permission.
- Do not perform unrelated refactoring during a small task.
- Do not change multiple modules unless the task requires it.
- Keep changes focused and reviewable.

## Testing Rules

For every new business function, consider:

- Successful case
- Resource not found
- Invalid input
- Business rule violation
- Database interaction
- HTTP status code
- Response body

Use:

- JUnit 5
- Mockito for unit tests
- Spring Boot testing tools for integration tests

Do not weaken or delete tests only to make a build pass.

## Build Commands

On Windows, use Maven Wrapper when available.

Compile the project:

```powershell
.\mvnw.cmd clean compile
```

Run tests:

```powershell
.\mvnw.cmd test
```

Run a clean verification:

```powershell
.\mvnw.cmd clean verify
```

Run the Spring Boot application:

```powershell
.\mvnw.cmd spring-boot:run
```

If Maven Wrapper is not available, use:

```powershell
mvn clean compile
mvn test
mvn clean verify
mvn spring-boot:run
```

Do not run database migrations, destructive SQL, or data deletion commands
without explicit permission.

## Codex Working Process

Before changing code:

1. Read this `AGENTS.md`.
2. Read `pom.xml`.
3. Read the relevant Java classes.
4. Read the relevant SQL files in `docs/database/public/`.
5. Read existing tests.
6. Explain the current behavior.
7. Propose a focused implementation plan.
8. List files expected to be modified.
9. Identify possible risks.
10. Wait for approval when the task changes database structure or public APIs.

While changing code:

- Make only the changes required for the task.
- Preserve the existing architecture.
- Do not modify unrelated modules.
- Do not change the database unless explicitly requested.
- Do not install new dependencies unless necessary and approved.
- Do not automatically commit code.
- Do not run destructive Git commands.
- Do not run destructive PostgreSQL commands.
- Do not hide errors or failed commands.

After changing code:

1. List all modified and created files.
2. Explain the important changes.
3. Run the relevant tests.
4. Run compilation when appropriate.
5. Report test and build results honestly.
6. Report any command that failed.
7. Report any remaining risk or unfinished work.
8. Do not commit changes automatically.

## Read-Only Analysis Mode

When a prompt says not to modify files:

- Do not create files.
- Do not edit files.
- Do not run Maven lifecycle commands that generate build output unless permitted.
- Do not run database migrations.
- Do not execute SQL that changes data.
- Do not install dependencies.
- Do not commit Git changes.

Allowed read-only activities include:

- Reading project files
- Listing directories
- Searching source code
- Reading Git status
- Reading SQL DDL
- Inspecting existing build output
- Reporting findings

## Definition of Done

A task is complete only when:

- The requested behavior is implemented.
- The project compiles successfully.
- Relevant tests pass.
- Entity mappings match PostgreSQL DDL.
- Business validation is implemented.
- HTTP status codes are appropriate.
- No credentials or sensitive data are exposed.
- No unrelated files are modified.
- All modified files are reported.
- Known limitations are clearly documented.