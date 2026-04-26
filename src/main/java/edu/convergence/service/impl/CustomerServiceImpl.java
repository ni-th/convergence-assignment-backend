package edu.convergence.service.impl;

import edu.convergence.repository.CustomerRepository;
import edu.convergence.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;

}
