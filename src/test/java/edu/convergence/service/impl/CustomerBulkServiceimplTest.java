package edu.convergence.service.impl;

import edu.convergence.dto.customer.CustomerBulkDTO;
import edu.convergence.entity.upload.UploadStatusEntity;
import edu.convergence.repository.CustomerBulkRepository;
import edu.convergence.repository.UploadStatusRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerBulkServiceimplTest {

    @Mock
    private CustomerBulkRepository bulkRepository;

    @Mock
    private UploadStatusRepository uploadStatusRepository;

    @InjectMocks
    private CustomerBulkServiceimpl customerBulkService;

    @Test
    void shouldProcessExcelAndUpsertParsedRows() throws Exception {
        MockMultipartFile file = excelFileWithRows(2);

        final List<CustomerBulkDTO> captured = new ArrayList<>();
        doAnswer(invocation -> {
            List<CustomerBulkDTO> input = invocation.getArgument(0);
            captured.addAll(input);
            return null;
        }).when(bulkRepository).upsertBatch(anyList());

        // Prepare upload status for async processing
        UploadStatusEntity status = UploadStatusEntity.builder()
                .id(1L)
                .uploadId("test-upload-id")
                .status("PENDING")
                .startTime(LocalDateTime.now())
                .processedRecords(0)
                .build();
        when(uploadStatusRepository.findById(1L)).thenReturn(Optional.of(status));
        when(uploadStatusRepository.save(any(UploadStatusEntity.class))).thenReturn(status);

        // Call async processor directly for testing
        customerBulkService.processExcelAsync(file, "test-upload-id", 1L);

        assertEquals(2, captured.size());
        assertEquals("900000001V", captured.get(0).getNic());
        assertEquals("Customer 1", captured.get(0).getName());
        assertEquals(LocalDate.of(1990, 1, 1), captured.get(0).getDateOfBirth());
        assertEquals("900000002V", captured.get(1).getNic());
    }

    @Test
    void shouldProcessExcelInBatchesOfHundred() throws Exception {
        MockMultipartFile file = excelFileWithRows(101);

        final List<Integer> batchSizes = new ArrayList<>();
        doAnswer(invocation -> {
            List<CustomerBulkDTO> input = invocation.getArgument(0);
            batchSizes.add(input.size());
            return null;
        }).when(bulkRepository).upsertBatch(anyList());

        // Prepare upload status for async processing
        UploadStatusEntity status = UploadStatusEntity.builder()
                .id(1L)
                .uploadId("test-upload-id")
                .status("PENDING")
                .startTime(LocalDateTime.now())
                .processedRecords(0)
                .build();
        when(uploadStatusRepository.findById(1L)).thenReturn(Optional.of(status));
        when(uploadStatusRepository.save(any(UploadStatusEntity.class))).thenReturn(status);

        // Call async processor directly for testing
        customerBulkService.processExcelAsync(file, "test-upload-id", 1L);

        // Async BATCH_SIZE is 500, so 101 rows produce a single batch of 101
        assertEquals(1, batchSizes.size());
        assertEquals(101, batchSizes.get(0));
    }

    @Test
    void shouldThrowWhenExcelCannotBeRead() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        IOException ioException = new IOException("broken file");
        when(file.getInputStream()).thenThrow(ioException);

        // Prepare upload status for async processing
        UploadStatusEntity status = UploadStatusEntity.builder()
                .id(1L)
                .uploadId("test-upload-id")
                .status("PENDING")
                .startTime(LocalDateTime.now())
                .processedRecords(0)
                .build();
        when(uploadStatusRepository.findById(1L)).thenReturn(Optional.of(status));

        // Call async processor which should catch exception and mark FAILED
        customerBulkService.processExcelAsync(file, "test-upload-id", 1L);

        ArgumentCaptor<UploadStatusEntity> captor = ArgumentCaptor.forClass(UploadStatusEntity.class);
        verify(uploadStatusRepository, atLeastOnce()).save(captor.capture());

        List<UploadStatusEntity> allCaptures = captor.getAllValues();
        UploadStatusEntity lastCapture = allCaptures.get(allCaptures.size() - 1);
        assertEquals("FAILED", lastCapture.getStatus());
        assertNotNull(lastCapture.getErrorMessage());
        assertTrue(lastCapture.getErrorMessage().contains("broken file"));
    }

    @Test
    void shouldReturnUploadIdForAsyncUpload() throws Exception {
        MockMultipartFile file = excelFileWithRows(2);

        UploadStatusEntity status = UploadStatusEntity.builder()
                .id(1L)
                .uploadId("test-upload-id")
                .status("PENDING")
                .startTime(LocalDateTime.now())
                .build();

        when(uploadStatusRepository.save(any(UploadStatusEntity.class)))
                .thenReturn(status);

        String uploadId = customerBulkService.uploadAsync(file);

        assertNotNull(uploadId);
        assertFalse(uploadId.isEmpty());
        verify(uploadStatusRepository, times(1)).save(any(UploadStatusEntity.class));
    }

    @Test
    void shouldCreatePendingStatusForAsyncUpload() throws Exception {
        MockMultipartFile file = excelFileWithRows(5);

        ArgumentCaptor<UploadStatusEntity> captor = ArgumentCaptor.forClass(UploadStatusEntity.class);

        UploadStatusEntity savedStatus = UploadStatusEntity.builder()
                .id(1L)
                .uploadId("test-id")
                .status("PENDING")
                .startTime(LocalDateTime.now())
                .build();

        when(uploadStatusRepository.save(any(UploadStatusEntity.class)))
                .thenReturn(savedStatus);

        customerBulkService.uploadAsync(file);

        verify(uploadStatusRepository).save(captor.capture());

        UploadStatusEntity captured = captor.getValue();
        assertEquals("PENDING", captured.getStatus());
        assertNotNull(captured.getStartTime());
        assertNull(captured.getEndTime());
    }

    @Test
    void shouldProcessAsyncWithStatusTracking() throws Exception {
        MockMultipartFile file = excelFileWithRows(100);

        UploadStatusEntity status = UploadStatusEntity.builder()
                .id(1L)
                .uploadId("test-upload-id")
                .status("PENDING")
                .startTime(LocalDateTime.now())
                .processedRecords(0)
                .build();

        when(uploadStatusRepository.findById(1L)).thenReturn(Optional.of(status));
        when(uploadStatusRepository.save(any(UploadStatusEntity.class))).thenReturn(status);
        doAnswer(invocation -> null).when(bulkRepository).upsertBatch(anyList());

        customerBulkService.processExcelAsync(file, "test-upload-id", 1L);

        verify(uploadStatusRepository, atLeast(2)).save(any(UploadStatusEntity.class));
        verify(bulkRepository, atLeastOnce()).upsertBatch(anyList());
    }

    @Test
    void shouldHandleAsyncProcessingError() throws Exception {
        MockMultipartFile file = excelFileWithRows(10);

        UploadStatusEntity status = UploadStatusEntity.builder()
                .id(1L)
                .uploadId("test-upload-id")
                .status("PENDING")
                .startTime(LocalDateTime.now())
                .processedRecords(0)
                .build();

        when(uploadStatusRepository.findById(1L)).thenReturn(Optional.of(status));
        when(uploadStatusRepository.save(any(UploadStatusEntity.class))).thenReturn(status);
        doThrow(new RuntimeException("DB Error")).when(bulkRepository).upsertBatch(anyList());

        customerBulkService.processExcelAsync(file, "test-upload-id", 1L);

        ArgumentCaptor<UploadStatusEntity> captor = ArgumentCaptor.forClass(UploadStatusEntity.class);
        verify(uploadStatusRepository, atLeast(1)).save(captor.capture());

        List<UploadStatusEntity> allCaptures = captor.getAllValues();
        UploadStatusEntity lastCapture = allCaptures.get(allCaptures.size() - 1);
        assertEquals("FAILED", lastCapture.getStatus());
        assertNotNull(lastCapture.getErrorMessage());
    }

    @Test
    void shouldMarkAsCompletedWhenAsyncProcessingSucceeds() throws Exception {
        MockMultipartFile file = excelFileWithRows(50);

        UploadStatusEntity status = UploadStatusEntity.builder()
                .id(1L)
                .uploadId("test-upload-id")
                .status("PENDING")
                .startTime(LocalDateTime.now())
                .processedRecords(0)
                .build();

        when(uploadStatusRepository.findById(1L)).thenReturn(Optional.of(status));
        when(uploadStatusRepository.save(any(UploadStatusEntity.class))).thenReturn(status);
        doAnswer(invocation -> null).when(bulkRepository).upsertBatch(anyList());

        customerBulkService.processExcelAsync(file, "test-upload-id", 1L);

        ArgumentCaptor<UploadStatusEntity> captor = ArgumentCaptor.forClass(UploadStatusEntity.class);
        verify(uploadStatusRepository, atLeast(1)).save(captor.capture());

        List<UploadStatusEntity> allCaptures = captor.getAllValues();
        UploadStatusEntity lastCapture = allCaptures.get(allCaptures.size() - 1);
        assertEquals("COMPLETED", lastCapture.getStatus());
        assertEquals(50, lastCapture.getTotalRecords());
        assertNotNull(lastCapture.getEndTime());
    }

    private MockMultipartFile excelFileWithRows(int dataRows) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("customers");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("nic");
        header.createCell(1).setCellValue("name");
        header.createCell(2).setCellValue("dateOfBirth");

        for (int i = 1; i <= dataRows; i++) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(String.format("90000000%dV", i % 10));
            row.createCell(1).setCellValue("Customer " + i);
            row.createCell(2).setCellValue(java.sql.Timestamp.valueOf(LocalDate.of(1990, 1, 1).atStartOfDay()));
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
                "file",
                "customers.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                outputStream.toByteArray()
        );
    }
}
