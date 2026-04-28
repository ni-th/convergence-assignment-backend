package edu.convergence.service.impl;

import edu.convergence.dto.customer.CustomerBulkDTO;
import edu.convergence.entity.upload.UploadStatusEntity;
import edu.convergence.repository.CustomerBulkRepository;
import edu.convergence.repository.UploadStatusRepository;
import edu.convergence.service.CustomerBulkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
@Service
@Slf4j
public class CustomerBulkServiceimpl implements CustomerBulkService {

    private final CustomerBulkRepository bulkRepository;
    private final UploadStatusRepository uploadStatusRepository;

    private static final int BATCH_SIZE = 500;
    private static final long TIMEOUT_SECONDS = 600; // 10 minutes

    @Override
    public String uploadAsync(MultipartFile file) {
        String uploadId = UUID.randomUUID().toString();

        UploadStatusEntity status = UploadStatusEntity.builder()
                .uploadId(uploadId)
                .status("PENDING")
                .startTime(LocalDateTime.now())
                .processedRecords(0)
                .build();
        uploadStatusRepository.save(status);

        log.info("Starting async upload with ID: {}", uploadId);
        processExcelAsync(file, uploadId, status.getId());

        return uploadId;
    }

    @Async("bulkUploadExecutor")
    public void processExcelAsync(MultipartFile file, String uploadId, Long statusId) {
        UploadStatusEntity status = uploadStatusRepository.findById(statusId).orElse(null);
        if (status == null) {
            log.error("Upload status not found for ID: {}", statusId);
            return;
        }

        try {
            status.setStatus("IN_PROGRESS");
            uploadStatusRepository.save(status);

            long startTime = System.currentTimeMillis();
            List<CustomerBulkDTO> batch = new ArrayList<>();
            int totalRecords = 0;

            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            log.info("Processing Excel with {} rows for upload ID: {}", sheet.getLastRowNum(), uploadId);

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                // Check timeout every batch
                if (i % BATCH_SIZE == 0) {
                    long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
                    if (elapsedSeconds > TIMEOUT_SECONDS) {
                        throw new TimeoutException("Bulk upload exceeded " + TIMEOUT_SECONDS +
                            " seconds. Processed " + totalRecords + " records in " + elapsedSeconds + "s");
                    }
                }

                Row row = sheet.getRow(i);

                // skip header
                if (i == 0 || row == null) {
                    continue;
                }

                try {
                    CustomerBulkDTO customer = new CustomerBulkDTO();
                    customer.setNic(row.getCell(0).getStringCellValue());
                    customer.setName(row.getCell(1).getStringCellValue());
                    customer.setDateOfBirth(row.getCell(2).getLocalDateTimeCellValue().toLocalDate());

                    batch.add(customer);
                    totalRecords++;

                    if (batch.size() >= BATCH_SIZE) {
                        bulkRepository.upsertBatch(batch);
                        status.setProcessedRecords(totalRecords);
                        uploadStatusRepository.save(status);
                        batch.clear();
                        log.info("Processed {} records for upload ID: {}", totalRecords, uploadId);
                    }
                } catch (Exception e) {
                    log.warn("Error processing row {} for upload ID {}: {}", i, uploadId, e.getMessage());
                    // Continue processing other rows
                    continue;
                }
            }

            // remaining records
            if (!batch.isEmpty()) {
                bulkRepository.upsertBatch(batch);
                status.setProcessedRecords(totalRecords);
            }

            status.setStatus("COMPLETED");
            status.setTotalRecords(totalRecords);
            status.setEndTime(LocalDateTime.now());
            uploadStatusRepository.save(status);

            workbook.close();

            long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            log.info("Upload ID {} completed successfully. Processed {} records in {}s",
                uploadId, totalRecords, elapsedSeconds);

        } catch (TimeoutException e) {
            status.setStatus("FAILED");
            status.setErrorMessage(e.getMessage());
            status.setEndTime(LocalDateTime.now());
            uploadStatusRepository.save(status);
            log.error("Upload ID {} timed out: {}", uploadId, e.getMessage());
        } catch (Exception e) {
            status.setStatus("FAILED");
            status.setErrorMessage(e.getMessage());
            status.setEndTime(LocalDateTime.now());
            uploadStatusRepository.save(status);
            log.error("Upload ID {} failed with error: {}", uploadId, e.getMessage(), e);
        }
    }
}
