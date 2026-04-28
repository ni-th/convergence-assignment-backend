package edu.convergence.controller;

import edu.convergence.dto.customer.CustomerDTO;
import edu.convergence.entity.upload.UploadStatusEntity;
import edu.convergence.repository.UploadStatusRepository;
import edu.convergence.service.CustomerBulkService;
import edu.convergence.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/customer")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CustomerController {
    private final CustomerService customerService;
    private final CustomerBulkService customerBulkService;
    private final UploadStatusRepository uploadStatusRepository;

    @GetMapping
    public ResponseEntity<Page<CustomerDTO>> getCustomers(@PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(customerService.getCustomers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.findById(id));
    }

    @PostMapping("/create-customer")
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        return ResponseEntity.ok(customerService.save(customerDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerDTO customerDTO) {
        return ResponseEntity.ok(customerService.update(id, customerDTO));
    }

    @PostMapping("/upload-async")
    public ResponseEntity<Map<String, String>> uploadExcelAsync(@RequestParam("file") MultipartFile file) {
        String uploadId = customerBulkService.uploadAsync(file);
        Map<String, String> response = new HashMap<>();
        response.put("uploadId", uploadId);
        response.put("message", "Upload started. Use upload ID to track progress.");
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping("/upload-status/{uploadId}")
    public ResponseEntity<?> getUploadStatus(@PathVariable String uploadId) {
        UploadStatusEntity status = uploadStatusRepository.findByUploadId(uploadId)
                .orElse(null);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }
}
