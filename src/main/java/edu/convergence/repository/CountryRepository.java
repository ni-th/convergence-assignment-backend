package edu.convergence.repository;

import edu.convergence.entity.location.CountryEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends CrudRepository<CountryEntity, Long> {
    Optional<CountryEntity> findByNameIgnoreCase(String name);
}

