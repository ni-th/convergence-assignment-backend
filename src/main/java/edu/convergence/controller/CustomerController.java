package edu.convergence.controller;

import edu.convergence.dto.customer.CustomerDTO;
import edu.convergence.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/customer")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping("/get-customer")
    public ResponseEntity<CustomerDTO> getCustomer() {
        return ResponseEntity.ok(new CustomerDTO());
    }
}
