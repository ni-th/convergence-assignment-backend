package edu.convergence.dto.customer;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerDTOValidationTest {

    private final Validator validator;

    CustomerDTOValidationTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    void shouldRejectMobileNumberThatDoesNotStartWithZero() {
        CustomerDTO customerDTO = validCustomerDTO();
        customerDTO.setMobileNumbers(Arrays.asList("1123456789"));

        Set<ConstraintViolation<CustomerDTO>> violations = validator.validate(customerDTO);

        assertTrue(violations.stream().anyMatch(violation -> violation.getPropertyPath().toString().contains("mobileNumbers")));
    }

    @Test
    void shouldRejectMobileNumberThatIsNotExactlyTenDigits() {
        CustomerDTO customerDTO = validCustomerDTO();
        customerDTO.setMobileNumbers(Arrays.asList("012345678"));

        Set<ConstraintViolation<CustomerDTO>> violations = validator.validate(customerDTO);

        assertTrue(violations.stream().anyMatch(violation -> violation.getPropertyPath().toString().contains("mobileNumbers")));
    }

    @Test
    void shouldAcceptMobileNumberThatStartsWithZeroAndHasTenDigits() {
        CustomerDTO customerDTO = validCustomerDTO();
        customerDTO.setMobileNumbers(Arrays.asList("0123456789"));

        Set<ConstraintViolation<CustomerDTO>> violations = validator.validate(customerDTO);

        assertTrue(violations.isEmpty());
    }

    private CustomerDTO validCustomerDTO() {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName("John Doe");
        customerDTO.setDateOfBirth(LocalDate.of(1990, 1, 1));
        customerDTO.setNic("901234567V");
        return customerDTO;
    }
}
