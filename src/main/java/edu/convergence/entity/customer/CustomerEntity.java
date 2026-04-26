package edu.convergence.entity.customer;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "customer")
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private String nic;

    @ElementCollection
    @CollectionTable(name = "customer_mobile_numbers", joinColumns = @JoinColumn(name = "customer_id"))
    private List<String> mobileNumbers;

    @OneToMany(mappedBy = "customer",cascade = CascadeType.ALL)
    private List<CustomerAddressEntity> addresses;

    @ManyToMany
    @JoinTable(
        name = "customer_family_members",
        joinColumns = @JoinColumn(name = "customer_id"),
        inverseJoinColumns = @JoinColumn(name = "family_member_id")
    )
    private List<CustomerEntity> familyMembers;

}