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

## Frontend Implementation Guide

### Technology Stack

Recommended: React + Axios + TypeScript

### 1. Install Dependencies

```bash
npm install axios react-query typescript
```

### 2. API Service

Create `src/services/customerApi.ts`:

```typescript
import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/customer';

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
});

export interface UploadResponse {
  uploadId: string;
  message: string;
}

export interface UploadStatus {
  id: number;
  uploadId: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  totalRecords: number | null;
  processedRecords: number | null;
  errorMessage: string | null;
  startTime: string;
  endTime: string | null;
}

// Start async upload
export const uploadExcelAsync = async (file: File): Promise<UploadResponse> => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await api.post<UploadResponse>('/upload-async', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

  return response.data;
};

// Get upload status
export const getUploadStatus = async (uploadId: string): Promise<UploadStatus> => {
  const response = await api.get<UploadStatus>(`/upload-status/${uploadId}`);
  return response.data;
};

// List customers
export const getCustomers = async (page = 0, size = 10) => {
  const response = await api.get(`?page=${page}&size=${size}`);
  return response.data;
};

// Get customer by ID
export const getCustomerById = async (id: number) => {
  const response = await api.get(`/${id}`);
  return response.data;
};

// Create customer
export const createCustomer = async (customerData: any) => {
  const response = await api.post('/create-customer', customerData);
  return response.data;
};

// Update customer
export const updateCustomer = async (id: number, customerData: any) => {
  const response = await api.put(`/${id}`, customerData);
  return response.data;
};
```

### 3. Bulk Upload Component

Create `src/components/BulkUploadModal.tsx`:

```typescript
import React, { useState } from 'react';
import { uploadExcelAsync, getUploadStatus, UploadStatus } from '../services/customerApi';

interface Props {
  onSuccess?: () => void;
}

const BulkUploadModal: React.FC<Props> = ({ onSuccess }) => {
  const [file, setFile] = useState<File | null>(null);
  const [uploadId, setUploadId] = useState<string | null>(null);
  const [status, setStatus] = useState<UploadStatus | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [uploadStarted, setUploadStarted] = useState(false);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files?.[0]) {
      setFile(e.target.files[0]);
      setError(null);
    }
  };

  const handleUpload = async () => {
    if (!file) {
      setError('Please select a file');
      return;
    }

    try {
      setLoading(true);
      const response = await uploadExcelAsync(file);
      setUploadId(response.uploadId);
      setUploadStarted(true);
      
      // Start polling
      pollStatus(response.uploadId);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Upload failed');
      setLoading(false);
    }
  };

  const pollStatus = async (id: string) => {
    const interval = setInterval(async () => {
      try {
        const uploadStatus = await getUploadStatus(id);
        setStatus(uploadStatus);

        if (uploadStatus.status === 'COMPLETED' || uploadStatus.status === 'FAILED') {
          clearInterval(interval);
          setLoading(false);
          if (uploadStatus.status === 'COMPLETED') {
            onSuccess?.();
          }
        }
      } catch (err) {
        clearInterval(interval);
        setError('Failed to fetch status');
        setLoading(false);
      }
    }, 2000); // Poll every 2 seconds
  };

  const progressPercent = status?.totalRecords
    ? Math.round((status.processedRecords! / status.totalRecords) * 100)
    : 0;

  return (
    <div className="upload-modal">
      <h2>Bulk Upload Customers</h2>

      {!uploadStarted ? (
        <>
          <input 
            type="file" 
            accept=".xlsx,.xls" 
            onChange={handleFileChange}
            disabled={loading}
          />
          <button onClick={handleUpload} disabled={!file || loading}>
            {loading ? 'Uploading...' : 'Upload'}
          </button>
        </>
      ) : (
        <>
          <div className="status-info">
            <p>Status: <strong>{status?.status}</strong></p>
            <p>Upload ID: {uploadId}</p>
            
            {status?.status === 'IN_PROGRESS' && (
              <>
                <div className="progress-bar">
                  <div
                    className="progress-fill"
                    style={{ width: `${progressPercent}%` }}
                  />
                </div>
                <p>
                  {status.processedRecords?.toLocaleString()} / {status.totalRecords?.toLocaleString()} records
                  ({progressPercent}%)
                </p>
              </>
            )}

            {status?.status === 'COMPLETED' && (
              <p className="success">
                ✓ Upload completed! {status.totalRecords?.toLocaleString()} records processed
              </p>
            )}

            {status?.status === 'FAILED' && (
              <p className="error">
                ✗ Upload failed: {status.errorMessage}
              </p>
            )}
          </div>
        </>
      )}

      {error && <p className="error">{error}</p>}
    </div>
  );
};

export default BulkUploadModal;
```

### 4. CSS Styling

```css
.upload-modal {
  border: 1px solid #ddd;
  padding: 20px;
  border-radius: 8px;
  max-width: 500px;
  margin: 20px auto;
  background: #f9f9f9;
}

.upload-modal h2 {
  margin-bottom: 20px;
  color: #333;
}

.upload-modal input[type="file"] {
  margin: 10px 0;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  width: 100%;
  box-sizing: border-box;
}

.upload-modal button {
  background: #007bff;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  width: 100%;
}

.upload-modal button:hover:not(:disabled) {
  background: #0056b3;
}

.upload-modal button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.progress-bar {
  width: 100%;
  height: 20px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  margin: 15px 0;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #28a745, #20c997);
  transition: width 0.3s ease;
}

.status-info {
  margin: 15px 0;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 4px;
  border-left: 4px solid #007bff;
}

.status-info p {
  margin: 5px 0;
  color: #333;
}

.error {
  color: #dc3545;
  margin-top: 10px;
  padding: 10px;
  background: #f8d7da;
  border-radius: 4px;
}

.success {
  color: #155724;
  margin-top: 10px;
  padding: 10px;
  background: #d4edda;
  border-radius: 4px;
}
```

### 5. Usage in App

```typescript
import React from 'react';
import BulkUploadModal from './components/BulkUploadModal';
import CustomerList from './components/CustomerList';

function App() {
  const [refresh, setRefresh] = React.useState(0);

  const handleUploadSuccess = () => {
    // Refresh customer list after successful upload
    setRefresh(prev => prev + 1);
  };

  return (
    <div className="app">
      <h1>Customer Management</h1>
      <BulkUploadModal onSuccess={handleUploadSuccess} />
      <CustomerList key={refresh} />
    </div>
  );
}

export default App;
```

### 6. Alternative: Vue 3 Implementation

Create `src/components/BulkUploadModal.vue`:

```vue
<template>
  <div class="upload-modal">
    <h2>Bulk Upload Customers</h2>

    <div v-if="!uploadStarted">
      <input 
        type="file" 
        accept=".xlsx,.xls" 
        @change="handleFileChange"
        :disabled="loading"
      />
      <button @click="handleUpload" :disabled="!file || loading">
        {{ loading ? 'Uploading...' : 'Upload' }}
      </button>
    </div>

    <div v-else class="status-info">
      <p>Status: <strong>{{ status?.status }}</strong></p>
      <p>Upload ID: {{ uploadId }}</p>

      <div v-if="status?.status === 'IN_PROGRESS'">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progressPercent + '%' }" />
        </div>
        <p>
          {{ (status.processedRecords || 0).toLocaleString() }} / 
          {{ (status.totalRecords || 0).toLocaleString() }} records ({{ progressPercent }}%)
        </p>
      </div>

      <p v-if="status?.status === 'COMPLETED'" class="success">
        ✓ Upload completed! {{ status.totalRecords?.toLocaleString() }} records processed
      </p>

      <p v-if="status?.status === 'FAILED'" class="error">
        ✗ Upload failed: {{ status.errorMessage }}
      </p>
    </div>

    <p v-if="error" class="error">{{ error }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { uploadExcelAsync, getUploadStatus } from '@/services/customerApi';

interface Props {
  onSuccess?: () => void;
}

defineProps<Props>();

const file = ref<File | null>(null);
const uploadId = ref<string | null>(null);
const status = ref<any>(null);
const loading = ref(false);
const error = ref<string | null>(null);
const uploadStarted = ref(false);

const progressPercent = computed(() => {
  if (!status.value?.totalRecords) return 0;
  return Math.round((status.value.processedRecords / status.value.totalRecords) * 100);
});

const handleFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement;
  if (input.files?.[0]) {
    file.value = input.files[0];
    error.value = null;
  }
};

const handleUpload = async () => {
  if (!file.value) {
    error.value = 'Please select a file';
    return;
  }

  try {
    loading.value = true;
    const response = await uploadExcelAsync(file.value);
    uploadId.value = response.uploadId;
    uploadStarted.value = true;
    pollStatus(response.uploadId);
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Upload failed';
    loading.value = false;
  }
};

const pollStatus = async (id: string) => {
  const interval = setInterval(async () => {
    try {
      const uploadStatus = await getUploadStatus(id);
      status.value = uploadStatus;

      if (uploadStatus.status === 'COMPLETED' || uploadStatus.status === 'FAILED') {
        clearInterval(interval);
        loading.value = false;
      }
    } catch {
      clearInterval(interval);
      error.value = 'Failed to fetch status';
      loading.value = false;
    }
  }, 2000);
};
</script>

<style scoped>
/* Same CSS as above */
</style>
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

