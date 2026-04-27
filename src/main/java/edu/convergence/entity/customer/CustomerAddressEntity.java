package edu.convergence.entity.customer;

import edu.convergence.entity.location.CityEntity;
import edu.convergence.entity.location.CountryEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class CustomerAddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String addressLine1;
    private String addressLine2;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private CityEntity city;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private CountryEntity country;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;
}
