package edu.convergence.service.impl;

import edu.convergence.dto.customer.CustomerDTO;
import edu.convergence.entity.customer.CustomerAddressEntity;
import edu.convergence.entity.customer.CustomerEntity;
import edu.convergence.entity.location.CityEntity;
import edu.convergence.entity.location.CountryEntity;
import edu.convergence.mapper.AddressMapper;
import edu.convergence.mapper.CustomerMapper;
import edu.convergence.repository.CityRepository;
import edu.convergence.repository.CountryRepository;
import edu.convergence.repository.CustomerRepository;
import edu.convergence.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;

    @Override
    public CustomerDTO save(CustomerDTO customerDTO) {
        validateUniqueNicForCreate(customerDTO.getNic());
        CustomerEntity customerEntity = CustomerMapper.toEntity(customerDTO);
        resolveLocations(customerEntity.getAddresses());
        CustomerEntity savedCustomer = customerRepository.save(customerEntity);
        return CustomerMapper.toDTO(savedCustomer);
    }

    @Override
    public CustomerDTO findById(Long id) {
        CustomerEntity customerEntity = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        return CustomerMapper.toDTO(customerEntity);
    }

    @Override
    @Transactional
    public CustomerDTO update(Long id, CustomerDTO customerDTO) {
        CustomerEntity existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));

        validateUniqueNicForUpdate(id, customerDTO.getNic());
        existingCustomer.setName(customerDTO.getName());
        existingCustomer.setDateOfBirth(customerDTO.getDateOfBirth());
        existingCustomer.setNic(customerDTO.getNic());
        existingCustomer.setMobileNumbers(customerDTO.getMobileNumbers() == null ? null : new ArrayList<>(customerDTO.getMobileNumbers()));
        existingCustomer.setFamilyMembers(customerDTO.getFamilyMembers() == null ? null : customerDTO.getFamilyMembers().stream()
                .map(CustomerMapper::toEntity)
                .collect(java.util.stream.Collectors.toList()));

        if (customerDTO.getAddresses() != null) {
            List<CustomerAddressEntity> mappedAddresses = AddressMapper.toEntity(customerDTO.getAddresses());
            resolveLocations(mappedAddresses);
            if (existingCustomer.getAddresses() == null) {
                existingCustomer.setAddresses(new ArrayList<>());
            } else {
                existingCustomer.getAddresses().clear();
            }
            for (CustomerAddressEntity address : mappedAddresses) {
                address.setCustomer(existingCustomer);
                existingCustomer.getAddresses().add(address);
            }
        }

        CustomerEntity updatedCustomer = customerRepository.save(existingCustomer);
        return CustomerMapper.toDTO(updatedCustomer);
    }

    @Override
    public Page<CustomerDTO> getCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(CustomerMapper::toDTO);
    }

    private void resolveLocations(List<CustomerAddressEntity> addresses) {
        if (addresses == null) {
            return;
        }

        for (CustomerAddressEntity address : addresses) {
            CountryEntity resolvedCountry = resolveCountry(address.getCountry());
            address.setCountry(resolvedCountry);
            address.setCity(resolveCity(address.getCity(), resolvedCountry));
        }
    }

    private CountryEntity resolveCountry(CountryEntity country) {
        if (country == null || isBlank(country.getName())) {
            return null;
        }

        String countryName = country.getName().trim();
        return countryRepository.findByNameIgnoreCase(countryName)
                .orElseGet(() -> {
                    CountryEntity newCountry = new CountryEntity();
                    newCountry.setName(countryName);
                    return countryRepository.save(newCountry);
                });
    }

    private CityEntity resolveCity(CityEntity city, CountryEntity country) {
        if (city == null || isBlank(city.getName())) {
            return null;
        }
        if (country == null) {
            throw new IllegalArgumentException("City requires a valid country");
        }

        String cityName = city.getName().trim();
        return cityRepository.findByNameIgnoreCaseAndCountry_Id(cityName, country.getId())
                .orElseGet(() -> {
                    CityEntity newCity = new CityEntity();
                    newCity.setName(cityName);
                    newCity.setCountry(country);
                    return cityRepository.save(newCity);
                });
    }

    private void validateUniqueNicForCreate(String nic) {
        String normalizedNic = normalizeNic(nic);
        if (customerRepository.existsByNicIgnoreCase(normalizedNic)) {
            throw new IllegalArgumentException("NIC already exists");
        }
    }

    private void validateUniqueNicForUpdate(Long id, String nic) {
        String normalizedNic = normalizeNic(nic);
        if (customerRepository.existsByNicIgnoreCaseAndIdNot(normalizedNic, id)) {
            throw new IllegalArgumentException("NIC already exists");
        }
    }

    private String normalizeNic(String nic) {
        return nic == null ? "" : nic.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
