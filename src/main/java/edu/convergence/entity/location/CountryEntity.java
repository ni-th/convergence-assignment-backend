package edu.convergence.entity.location;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "country")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class CountryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
}
