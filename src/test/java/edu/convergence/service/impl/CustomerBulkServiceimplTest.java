package edu.convergence.service.impl;

import edu.convergence.dto.customer.CustomerBulkDTO;
import edu.convergence.repository.CustomerBulkRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerBulkServiceimplTest {

    @Mock
    private CustomerBulkRepository bulkRepository;

    @InjectMocks
    private CustomerBulkServiceimpl customerBulkService;

    @Test
    void shouldProcessExcelAndUpsertParsedRows() throws Exception {
        MockMultipartFile file = excelFileWithRows(2);

        final List<CustomerBulkDTO> captured = new ArrayList<CustomerBulkDTO>();
        doAnswer(invocation -> {
            List<CustomerBulkDTO> input = invocation.getArgument(0);
            captured.addAll(input);
            return null;
        }).when(bulkRepository).upsertBatch(anyList());

        customerBulkService.processExcel(file);

        assertEquals(2, captured.size());
        assertEquals("900000001V", captured.get(0).getNic());
        assertEquals("Customer 1", captured.get(0).getName());
        assertEquals(LocalDate.of(1990, 1, 1), captured.get(0).getDateOfBirth());
        assertEquals("900000002V", captured.get(1).getNic());
    }

    @Test
    void shouldProcessExcelInBatchesOfHundred() throws Exception {
        MockMultipartFile file = excelFileWithRows(101);

        final List<Integer> batchSizes = new ArrayList<Integer>();
        doAnswer(invocation -> {
            List<CustomerBulkDTO> input = invocation.getArgument(0);
            batchSizes.add(input.size());
            return null;
        }).when(bulkRepository).upsertBatch(anyList());

        customerBulkService.processExcel(file);

        assertEquals(2, batchSizes.size());
        assertEquals(100, batchSizes.get(0));
        assertEquals(1, batchSizes.get(1));
    }

    @Test
    void shouldThrowWhenExcelCannotBeRead() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        IOException ioException = new IOException("broken file");
        when(file.getInputStream()).thenThrow(ioException);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> customerBulkService.processExcel(file));

        assertEquals("Excel processing failed", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("broken file", exception.getCause().getMessage());
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
            row.createCell(0).setCellValue(String.format("90000000%dV", i));
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

