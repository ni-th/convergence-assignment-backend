# Customer Management Backend

Spring Boot backend for customer management with CRUD APIs, paging, Excel bulk upload with async processing and timeouts, validation, and Swagger/OpenAPI docs.

## Tech Stack

- Java 8
- Spring Boot 2.7.18
- Spring Web
- Spring Data JPA
- Spring JDBC
- Spring Boot Starter Test
- MariaDB
- Apache POI (`poi-ooxml`) for Excel parsing
- Springdoc OpenAPI UI
- JUnit 5 + Mockito

## Setup & Run

### Prerequisites
- Java 8+
- Maven 3.6+
- MariaDB 10.4+

### Configuration

Update `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3308/cms?createDatabaseIfNotExist=true
    username: root
    password: 1234
    driver-class-name: org.mariadb.jdbc.Driver
```

### Run Application

```powershell
mvn spring-boot:run
```

Server starts on: `http://localhost:8080`

API Docs: `http://localhost:8080/swagger-ui/index.html`

## REST API Endpoints

### Customer CRUD

- `GET /api/customer` - List customers (paginated)
- `GET /api/customer/{id}` - Get customer by ID
- `POST /api/customer/create-customer` - Create customer
- `PUT /api/customer/{id}` - Update customer

**Create/Update Payload:**

```json
{
  "id": 1,
  "name": "John Doe",
  "dateOfBirth": "1990-01-01",
  "nic": "901234567V",
  "mobileNumbers": ["0771234567"],
  "addresses": [
    {
      "addressLine1": "123 Main St",
      "city": { "city": "Colombo" },
      "country": { "country": "Sri Lanka" }
    }
  ]
}
```

### Bulk Upload (Async Only)

#### 1. Start Async Upload

```http
POST /api/customer/upload-async
Content-Type: multipart/form-data

file: [Excel file]
```

**Response (202 ACCEPTED):**

```json
{
  "uploadId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Upload started. Use upload ID to track progress."
}
```

#### 2. Check Upload Status

```http
GET /api/customer/upload-status/{uploadId}
```

**Response (200 OK):**

```json
{
  "id": 1,
  "uploadId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "IN_PROGRESS",
  "totalRecords": null,
  "processedRecords": 25000,
  "errorMessage": null,
  "startTime": "2026-04-28T10:30:15.123456",
  "endTime": null
}
```

**Status Values:**
- `PENDING` - Queued, not yet started
- `IN_PROGRESS` - Currently processing
- `COMPLETED` - All records processed
- `FAILED` - Upload failed (check errorMessage)

### Excel File Format

Required columns:
1. **NIC** - Customer ID (9 digits + V/X or 12 digits)
2. **Name** - Full name
3. **Date of Birth** - YYYY-MM-DD format

Example:

```
NIC              | Name           | Date of Birth
900000001V       | John Doe       | 1990-01-15
900000002V       | Jane Smith     | 1985-03-22
```

## Validation Rules

- `name`: Required, non-blank
- `dateOfBirth`: Must be in the past
- `nic`: Required, unique, format: 9 digits + V/X or 12 digits
- `mobileNumbers`: Required, non-empty list, format: 0XXXXXXXXX

## Bulk Upload Specifications

- **Processing**: Background async with timeout
- **Timeout**: 10 minutes per upload
- **Batch Size**: 500 records
- **Concurrent Uploads**: Up to 5
- **Queue Capacity**: 100 pending uploads
- **Performance (1M records)**: 3-5 minutes
- **Request Response**: < 1 second (non-blocking)

## Error Handling

**Validation Errors (400 Bad Request):**

```json
{
  "field1": "error message",
  "field2": "error message"
}
```

**Business Errors (409 Conflict):**

```json
{
  "message": "NIC already exists"
}
```

**Upload Errors:**

Check upload status endpoint for detailed error message in `errorMessage` field.

## Testing

```powershell
# Run all tests
mvn test

# Run bulk upload tests
mvn -Dtest=CustomerBulkServiceimplTest test
```

## Key Frontend Implementation Points

1. **Non-blocking UX** - Upload returns immediately, show upload ID to user
2. **Poll every 2 seconds** - Check status until COMPLETED or FAILED
3. **Progress display** - Show percentage based on processedRecords/totalRecords
4. **Error handling** - Display errorMessage from status endpoint
5. **Excel format** - Validate file is .xlsx/.xls
6. **File size validation** - Limit file size to 100MB (browser level)
7. **User feedback** - Show success/error messages clearly
8. **Auto-refresh list** - Refresh customer list after successful upload

## Notes

- CORS enabled for `http://localhost:5173` (adjust for your frontend URL)
- Update backend CORS origin in `CustomerController.java` if needed
- All endpoints use JSON request/response
- Async upload uses multipart/form-data for file upload

