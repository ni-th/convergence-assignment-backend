package edu.convergence.service;

import edu.convergence.dto.customer.CustomerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    CustomerDTO save(CustomerDTO customerDTO);
    CustomerDTO findById(Long id);
    CustomerDTO update(Long id, CustomerDTO customerDTO);
    Page<CustomerDTO> getCustomers(Pageable pageable);
}
