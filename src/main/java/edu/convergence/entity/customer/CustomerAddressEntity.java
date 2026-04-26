package edu.convergence.entity.customer;

import edu.convergence.entity.location.CityEntity;

import javax.persistence.*;

@Entity
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
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;
}
