# Customer Management Backend

Spring Boot backend for customer management with CRUD APIs, paging, Excel bulk upload, validation, and Swagger/OpenAPI docs.

## Checklist

- [x] Project overview and stack
- [x] Setup and configuration
- [x] Run instructions
- [x] API endpoint reference with sample payloads
- [x] Validation and error handling behavior
- [x] Bulk upload flow
- [x] Testing guide
- [x] Package structure

## Tech Stack

- Java 8
- Spring Boot 2.7.18
- Spring Web
- Spring Data JPA
- Spring JDBC
- Spring Boot Starter Test (test scope)
- MariaDB
- Apache POI (`poi-ooxml`) for Excel parsing
- Springdoc OpenAPI UI
- JUnit 5 + Mockito

## Project Layout

```text
backend/
  pom.xml
  src/main/java/edu/convergence/
    Main.java
    config/OpenApiConfig.java
    controller/CustomerController.java
    dto/
    entity/
    exception/GlobleExceptionHandler.java
    mapper/
    repository/
    service/
  src/main/resources/application.yml
  src/test/java/edu/convergence/
```

## Configuration

Current defaults from `src/main/resources/application.yml`:

- Server port: `8080`
- DB URL: `jdbc:mariadb://localhost:3308/cms?createDatabaseIfNotExist=true`
- DB user: `root`
- DB password: `1234`
- JPA `ddl-auto`: `update`
- SQL logging: enabled (`show-sql: true`, formatted SQL enabled)

Before running in another environment, update DB values as needed.

## Run the Application

From project root (`E:\Convergence Assignment\backend`):

```powershell
mvn spring-boot:run
```

Or run the main class:

- `edu.convergence.Main`

## API Documentation

Configured in `OpenApiConfig` with title **Customer API**.

When app is running, check:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## REST API Endpoints

Base path: `/api/customer`

### 1) Get paged customers

- `GET /api/customer`
- Supports Spring pageable params (`page`, `size`, `sort`)
- Default sort is `id`

Example:

```http
GET /api/customer?page=0&size=10&sort=id,asc
```

### 2) Get customer by id

- `GET /api/customer/{id}`

### 3) Create customer

- `POST /api/customer/create-customer`
- Body: `CustomerDTO`

Sample JSON:

```json
{
  "name": "John Doe",
  "dateOfBirth": "1990-01-01",
  "nic": "901234567V",
  "mobileNumbers": ["0771234567"],
  "familyMembers": [],
  "addresses": [
    {
      "addressLine1": "12 Main Street",
      "addressLine2": "Apt 4",
      "city": { "city": "Colombo" },
      "country": { "country": "Sri Lanka" }
    }
  ]
}
```

### 4) Update customer

- `PUT /api/customer/{id}`
- Body: `CustomerDTO`
- Service resolves customer by path `id` and updates fields.

### 5) Bulk upload from Excel

- `POST /api/customer/upload`
- `multipart/form-data`
- File field name must be `file`

Example:

```http
POST /api/customer/upload
Content-Type: multipart/form-data
```

Returns `200 OK` with message `Upload successful` when processing completes.

## Validation Rules

From `CustomerDTO` and `CustomerBulkDTO`:

- `name`: required (`@NotBlank`)
- `dateOfBirth`: must be in the past (`@Past`)
- `nic`: required and must match:
  - `9 digits + V/X` (case-insensitive), or
  - `12 digits`
- `mobileNumbers`: required non-empty list
- each mobile number must match `^0\d{9}$` (starts with `0`, exactly 10 digits)

## NIC Uniqueness Behavior

In service layer:

- On create: rejects if NIC already exists
- On update: rejects if NIC belongs to another customer
- Error: `IllegalArgumentException("NIC already exists")`

## Error Handling

`GlobleExceptionHandler` behavior:

- Validation failure (`MethodArgumentNotValidException`):
  - HTTP `400 Bad Request`
  - Response body map of field -> message
- Business conflict (`IllegalArgumentException`):
  - HTTP `409 Conflict`
  - Response body: `{ "message": "..." }`

## Bulk Upload Details

`CustomerBulkServiceimpl.processExcel(...)`:

- Reads first sheet from uploaded `.xlsx`
- Skips row `0` as header
- Maps columns:
  - `0` -> NIC
  - `1` -> Name
  - `2` -> Date of birth
- Upserts in batches of `100`
- Uses JDBC batch with SQL `INSERT ... ON DUPLICATE KEY UPDATE`
- Wraps parse/read failures as `RuntimeException("Excel processing failed", cause)`

## Running Tests

Run all tests:

```powershell
mvn test
```

Run specific tests:

```powershell
mvn -Dtest=CustomerServiceImplCrudTest test
mvn -Dtest=CustomerServiceImplPagingTest test
mvn -Dtest=CustomerBulkServiceimplTest test
mvn -Dtest=CustomerMapperTest test
mvn -Dtest=CustomerDTOValidationTest test
```

Current test classes:

- `src/test/java/edu/convergence/service/impl/CustomerServiceImplCrudTest.java`
- `src/test/java/edu/convergence/service/impl/CustomerServiceImplPagingTest.java`
- `src/test/java/edu/convergence/service/impl/CustomerBulkServiceimplTest.java`
- `src/test/java/edu/convergence/mapper/CustomerMapperTest.java`
- `src/test/java/edu/convergence/dto/customer/CustomerDTOValidationTest.java`

Test reports are generated under `target/surefire-reports`.

## Notes

- CORS currently allows `http://localhost:5173` in `CustomerController`.
- If Maven is not available in your terminal, install Maven or run from an IDE Maven tool window.
