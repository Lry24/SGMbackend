package com.sgm.SGMbackend.mapper;

import com.sgm.SGMbackend.dto.dtoRequest.BaremeRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.BaremeResponseDTO;
import com.sgm.SGMbackend.entity.Bareme;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BaremeMapper {
    BaremeResponseDTO toResponseDTO(Bareme entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Bareme toEntity(BaremeRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(BaremeRequestDTO requestDTO, @MappingTarget Bareme entity);
}
