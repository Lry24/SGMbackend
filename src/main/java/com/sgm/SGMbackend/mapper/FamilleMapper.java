package com.sgm.SGMbackend.mapper;

import com.sgm.SGMbackend.dto.dtoRequest.FamilleRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.FamilleResponseDTO;
import com.sgm.SGMbackend.entity.Famille;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FamilleMapper {

    FamilleResponseDTO toResponseDTO(Famille entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actif", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Famille toEntity(FamilleRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actif", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(FamilleRequestDTO requestDTO, @MappingTarget Famille entity);
}
