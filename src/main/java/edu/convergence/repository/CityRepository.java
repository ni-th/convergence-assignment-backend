package edu.convergence.repository;

import edu.convergence.entity.location.CityEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CityRepository extends CrudRepository<CityEntity, Long> {
    Optional<CityEntity> findByNameIgnoreCaseAndCountry_Id(String name, Long countryId);
}

