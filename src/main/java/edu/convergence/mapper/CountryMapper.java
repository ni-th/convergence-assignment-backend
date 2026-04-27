package edu.convergence.mapper;

import edu.convergence.dto.location.CountryDTO;
import edu.convergence.entity.location.CountryEntity;

public class CountryMapper {
    public static CountryDTO toDTO(CountryEntity entity) {
        if (entity == null) {
            return null;
        }
        return CountryDTO.builder()
                .country(entity.getName())
                .build();
    }
    public static CountryEntity toEntity(CountryDTO dto) {
        if (dto == null) {
            return null;
        }
        return CountryEntity.builder()
                .name(dto.getCountry())
                .build();
    }
}
