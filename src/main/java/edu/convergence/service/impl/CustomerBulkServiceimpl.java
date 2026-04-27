package edu.convergence.service.impl;

import edu.convergence.dto.customer.CustomerBulkDTO;
import edu.convergence.repository.CustomerBulkRepository;
import edu.convergence.service.CustomerBulkService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomerBulkServiceimpl implements CustomerBulkService {

    private final CustomerBulkRepository bulkRepository;
    private static final int BATCH_SIZE = 100;

    @Override
    public void processExcel(MultipartFile file) {
        List<CustomerBulkDTO> batch = new ArrayList<CustomerBulkDTO>();
        try {

            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                // skip header
                if (i == 0) {
                    continue;
                }

                CustomerBulkDTO customer = new CustomerBulkDTO();
                customer.setNic(row.getCell(0).getStringCellValue());
                customer.setName(row.getCell(1).getStringCellValue());

                customer.setDateOfBirth(row.getCell(2).getLocalDateTimeCellValue().toLocalDate());

                batch.add(customer);

                if (batch.size() == BATCH_SIZE) {
                    bulkRepository.upsertBatch(batch);
                    batch.clear();
                }
            }

            // remaining records
            if (!batch.isEmpty()) {
                bulkRepository.upsertBatch(batch);
            }

            workbook.close();

        } catch (Exception e) {
            throw new RuntimeException("Excel processing failed", e);
        }
    }
}
