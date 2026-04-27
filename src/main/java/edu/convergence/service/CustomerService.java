package edu.convergence.service;

import edu.convergence.dto.customer.CustomerDTO;

public interface CustomerService {
    CustomerDTO save(CustomerDTO customerDTO);
    CustomerDTO findById(Long id);
    CustomerDTO update(Long id, CustomerDTO customerDTO);
}
