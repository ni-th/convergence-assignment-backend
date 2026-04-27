package edu.convergence.dto.customer;

import edu.convergence.dto.location.CityDTO;
import edu.convergence.dto.location.CountryDTO;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class CustomerAddressDTO {
    private String addressLine1;
    private String addressLine2;
    private CityDTO city;
    private CountryDTO country;
}
