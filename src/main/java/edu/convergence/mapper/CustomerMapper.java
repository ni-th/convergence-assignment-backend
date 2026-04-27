package edu.convergence.mapper;

import edu.convergence.dto.customer.CustomerDTO;
import edu.convergence.entity.customer.CustomerEntity;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class CustomerMapper {

    private CustomerMapper() {
    }

    public static CustomerDTO toDTO(CustomerEntity customerEntity) {
        if (customerEntity == null) {
            return null;
        }

        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName(customerEntity.getName());
        customerDTO.setDateOfBirth(customerEntity.getDateOfBirth());
        customerDTO.setNic(customerEntity.getNic());
        customerDTO.setMobileNumbers(customerEntity.getMobileNumbers() == null ? null : new ArrayList<>(customerEntity.getMobileNumbers()));
        customerDTO.setFamilyMembers(customerEntity.getFamilyMembers() == null ? null : customerEntity.getFamilyMembers().stream()
                .map(CustomerMapper::toDTO)
                .collect(Collectors.toList()));
        customerDTO.setAddresses(AddressMapper.toDTO(customerEntity.getAddresses()));
        return customerDTO;
    }

    public static CustomerEntity toEntity(CustomerDTO customerDTO) {
        if (customerDTO == null) {
            return null;
        }

        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setName(customerDTO.getName());
        customerEntity.setDateOfBirth(customerDTO.getDateOfBirth());
        customerEntity.setNic(customerDTO.getNic());
        customerEntity.setMobileNumbers(customerDTO.getMobileNumbers() == null ? null : new ArrayList<>(customerDTO.getMobileNumbers()));
        customerEntity.setFamilyMembers(customerDTO.getFamilyMembers() == null ? null : customerDTO.getFamilyMembers().stream()
                .map(CustomerMapper::toEntity)
                .collect(Collectors.toList()));
        customerEntity.setAddresses(AddressMapper.toEntity(customerDTO.getAddresses()));
        return customerEntity;
    }
}
