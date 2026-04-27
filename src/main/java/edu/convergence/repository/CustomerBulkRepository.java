package edu.convergence.repository;

import edu.convergence.dto.customer.CustomerBulkDTO;

import java.util.List;

public interface CustomerBulkRepository {
    void upsertBatch(List<CustomerBulkDTO> customers);
}
