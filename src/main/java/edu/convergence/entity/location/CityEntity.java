package edu.convergence.entity.location;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "city")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class CityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private CountryEntity country;
}
