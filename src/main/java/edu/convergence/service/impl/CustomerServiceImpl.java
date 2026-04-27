package edu.convergence.service.impl;

import edu.convergence.dto.customer.CustomerDTO;
import edu.convergence.entity.customer.CustomerEntity;
import edu.convergence.mapper.CustomerMapper;
import edu.convergence.repository.CustomerRepository;
import edu.convergence.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;

    @Override
    public CustomerDTO save(CustomerDTO customerDTO) {
        CustomerEntity customerEntity = CustomerMapper.toEntity(customerDTO);
        CustomerEntity savedCustomer = customerRepository.save(customerEntity);
        return CustomerMapper.toDTO(savedCustomer);
    }
}
