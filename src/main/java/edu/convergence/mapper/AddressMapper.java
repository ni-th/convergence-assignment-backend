package edu.convergence.mapper;

import edu.convergence.dto.customer.CustomerAddressDTO;
import edu.convergence.entity.customer.CustomerAddressEntity;

import java.util.List;
import java.util.stream.Collectors;

public class AddressMapper {

    private AddressMapper() {
    }

    public static List<CustomerAddressDTO> toDTO(List<CustomerAddressEntity> customerAddressEntities) {
        if (customerAddressEntities == null) {
            return null;
        }
        return customerAddressEntities.stream()
                .map(customerAddressEntity -> {
                    CustomerAddressDTO customerAddressDTO = new CustomerAddressDTO();
                    customerAddressDTO.setAddressLine1(customerAddressEntity.getAddressLine1());
                    customerAddressDTO.setAddressLine2(customerAddressEntity.getAddressLine2());
                    customerAddressDTO.setCity(CityMapper.toDTO(customerAddressEntity.getCity()));
                    customerAddressDTO.setCountry(CountryMapper.toDTO(customerAddressEntity.getCountry()));
                    return customerAddressDTO;
                })
                .collect(Collectors.toList());
    }

    public static List<CustomerAddressEntity> toEntity(List<CustomerAddressDTO> customerAddressDTOs) {
        if (customerAddressDTOs == null) {
            return null;
        }
        return customerAddressDTOs.stream()
                .map(customerAddressDTO -> {
                    CustomerAddressEntity customerAddressEntity = new CustomerAddressEntity();
                    customerAddressEntity.setAddressLine1(customerAddressDTO.getAddressLine1());
                    customerAddressEntity.setAddressLine2(customerAddressDTO.getAddressLine2());
                    customerAddressEntity.setCity(CityMapper.toEntity(customerAddressDTO.getCity()));
                    customerAddressEntity.setCountry(CountryMapper.toEntity(customerAddressDTO.getCountry()));
                    return customerAddressEntity;
                })
                .collect(Collectors.toList());
    }
}
