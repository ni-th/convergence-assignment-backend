package edu.convergence.service;

import org.springframework.web.multipart.MultipartFile;

public interface CustomerBulkService {
    String uploadAsync(MultipartFile file);
}
