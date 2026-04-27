package edu.convergence.mapper;

import edu.convergence.dto.customer.CustomerDTO;
import edu.convergence.entity.customer.CustomerAddressEntity;
import edu.convergence.entity.customer.CustomerEntity;
import edu.convergence.entity.location.CityEntity;
import edu.convergence.entity.location.CountryEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CustomerMapperTest {

    @Test
    void shouldMapEntityToDtoAndBack() {
        CountryEntity countryEntity = new CountryEntity();
        countryEntity.setName("Sri Lanka");

        CityEntity cityEntity = new CityEntity();
        cityEntity.setName("Colombo");
        cityEntity.setCountry(countryEntity);

        CustomerAddressEntity addressEntity = new CustomerAddressEntity();
        addressEntity.setAddressLine1("12 Main Street");
        addressEntity.setAddressLine2("Apt 4");
        addressEntity.setCity(cityEntity);
        addressEntity.setCountry(countryEntity);

        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setName("John Doe");
        customerEntity.setDateOfBirth(LocalDate.of(1990, 1, 1));
        customerEntity.setNic("901234567V");
        customerEntity.setMobileNumbers(Collections.singletonList("0123456789"));
        customerEntity.setAddresses(Collections.singletonList(addressEntity));
        customerEntity.setFamilyMembers(null);

        CustomerDTO customerDTO = CustomerMapper.toDTO(customerEntity);

        assertNotNull(customerDTO);
        assertEquals("John Doe", customerDTO.getName());
        assertEquals("0123456789", customerDTO.getMobileNumbers().get(0));
        assertEquals("12 Main Street", customerDTO.getAddresses().get(0).getAddressLine1());
        assertEquals("Colombo", customerDTO.getAddresses().get(0).getCity().getCity());
        assertEquals("Sri Lanka", customerDTO.getAddresses().get(0).getCountry().getCountry());

        CustomerEntity mappedBackEntity = CustomerMapper.toEntity(customerDTO);
        assertNotNull(mappedBackEntity);
        assertEquals("John Doe", mappedBackEntity.getName());
        assertEquals("0123456789", mappedBackEntity.getMobileNumbers().get(0));
        assertEquals("12 Main Street", mappedBackEntity.getAddresses().get(0).getAddressLine1());
        assertEquals("Colombo", mappedBackEntity.getAddresses().get(0).getCity().getName());
        assertEquals("Sri Lanka", mappedBackEntity.getAddresses().get(0).getCountry().getName());
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        assertNull(CustomerMapper.toDTO(null));
        assertNull(CustomerMapper.toEntity(null));
    }
}
