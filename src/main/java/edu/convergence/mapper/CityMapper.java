package edu.convergence.mapper;

import edu.convergence.dto.location.CityDTO;
import edu.convergence.entity.location.CityEntity;

public class CityMapper {
    public static CityDTO toDTO(CityEntity cityEntity) {
        if (cityEntity == null) {
            return null;
        }
        return CityDTO.builder()
                .city(cityEntity.getName())
                .build();
    }
    public static CityEntity toEntity(CityDTO cityDTO) {
        if (cityDTO == null) {
            return null;
        }
        CityEntity cityEntity = new CityEntity();
        cityEntity.setName(cityDTO.getCity());
        return cityEntity;
    }
}
