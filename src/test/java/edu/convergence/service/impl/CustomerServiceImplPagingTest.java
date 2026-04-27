package edu.convergence.service.impl;

import edu.convergence.entity.customer.CustomerEntity;
import edu.convergence.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplPagingTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void shouldReturnPagedCustomers() {
        CustomerEntity first = new CustomerEntity();
        first.setId(1L);
        first.setName("Alice");
        first.setDateOfBirth(LocalDate.of(1990, 1, 1));
        first.setNic("901234567V");
        first.setMobileNumbers(Collections.singletonList("0123456789"));

        CustomerEntity second = new CustomerEntity();
        second.setId(2L);
        second.setName("Bob");
        second.setDateOfBirth(LocalDate.of(1991, 2, 2));
        second.setNic("911234567V");
        second.setMobileNumbers(Collections.singletonList("0123456790"));

        Page<CustomerEntity> page = new PageImpl<>(Arrays.asList(first, second), PageRequest.of(0, 2), 2);
        when(customerRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<?> result = customerService.getCustomers(PageRequest.of(0, 2));

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("Alice", ((edu.convergence.dto.customer.CustomerDTO) result.getContent().get(0)).getName());
        assertEquals("Bob", ((edu.convergence.dto.customer.CustomerDTO) result.getContent().get(1)).getName());
    }
}
