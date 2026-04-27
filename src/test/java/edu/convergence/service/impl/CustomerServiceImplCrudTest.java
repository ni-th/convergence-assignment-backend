package edu.convergence.service.impl;

import edu.convergence.dto.customer.CustomerDTO;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplCrudTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void shouldSaveCustomer() {
        CustomerDTO request = validCustomerDTO();

        CustomerEntity savedEntity = new CustomerEntity();
        savedEntity.setId(100L);
        savedEntity.setName(request.getName());
        savedEntity.setDateOfBirth(request.getDateOfBirth());
        savedEntity.setNic(request.getNic());
        savedEntity.setMobileNumbers(request.getMobileNumbers());

        when(customerRepository.existsByNicIgnoreCase("901234567V")).thenReturn(false);
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(savedEntity);

        CustomerDTO result = customerService.save(request);

        assertEquals(100L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("901234567V", result.getNic());
        verify(customerRepository).existsByNicIgnoreCase("901234567V");
        verify(customerRepository).save(any(CustomerEntity.class));
    }

    @Test
    void shouldFindCustomerById() {
        CustomerEntity entity = new CustomerEntity();
        entity.setId(10L);
        entity.setName("Alice");
        entity.setDateOfBirth(LocalDate.of(1992, 6, 15));
        entity.setNic("921234567V");
        entity.setMobileNumbers(Collections.singletonList("0771234567"));

        when(customerRepository.findById(10L)).thenReturn(Optional.of(entity));

        CustomerDTO result = customerService.findById(10L);

        assertEquals(10L, result.getId());
        assertEquals("Alice", result.getName());
        assertEquals("921234567V", result.getNic());
    }

    @Test
    void shouldUpdateCustomer() {
        CustomerEntity existing = new CustomerEntity();
        existing.setId(10L);
        existing.setName("Old Name");
        existing.setDateOfBirth(LocalDate.of(1985, 1, 1));
        existing.setNic("851234567V");
        existing.setMobileNumbers(Collections.singletonList("0711111111"));

        CustomerDTO request = validCustomerDTO();
        request.setName("Updated Name");
        request.setNic("991234567V");
        request.setMobileNumbers(Collections.singletonList("0787654321"));

        when(customerRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(customerRepository.existsByNicIgnoreCaseAndIdNot("991234567V", 10L)).thenReturn(false);
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerDTO result = customerService.update(10L, request);

        assertEquals(10L, result.getId());
        assertEquals("Updated Name", result.getName());
        assertEquals("991234567V", result.getNic());
        assertEquals("0787654321", result.getMobileNumbers().get(0));
        verify(customerRepository).existsByNicIgnoreCaseAndIdNot("991234567V", 10L);
        verify(customerRepository).save(existing);
    }

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

        Page<CustomerDTO> result = customerService.getCustomers(PageRequest.of(0, 2));

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("Alice", result.getContent().get(0).getName());
        assertEquals("Bob", result.getContent().get(1).getName());
        verify(customerRepository).findAll(eq(PageRequest.of(0, 2)));
    }

    @Test
    void shouldThrowWhenCustomerNotFoundById() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> customerService.findById(999L));

        assertEquals("Customer not found with id: 999", ex.getMessage());
    }

    private CustomerDTO validCustomerDTO() {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName("John Doe");
        customerDTO.setDateOfBirth(LocalDate.of(1990, 1, 1));
        customerDTO.setNic("901234567V");
        customerDTO.setMobileNumbers(Collections.singletonList("0771234567"));
        return customerDTO;
    }
}

