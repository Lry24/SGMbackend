package com.sgm.SGMbackend.mapper;

import com.sgm.SGMbackend.dto.dtoRequest.ChambreFroideRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.ChambreFroideResponseDTO;
import com.sgm.SGMbackend.entity.ChambreFroide;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = { EmplacementMapper.class })
public interface ChambreFroideMapper {

    @Mapping(target = "nombreOccupes", expression = "java(entity.getEmplacements() != null ? (int) entity.getEmplacements().stream().filter(e -> Boolean.TRUE.equals(e.getOccupe())).count() : 0)")
    @Mapping(target = "nombreDisponibles", expression = "java(entity.getCapacite() - (entity.getEmplacements() != null ? (int) entity.getEmplacements().stream().filter(e -> Boolean.TRUE.equals(e.getOccupe())).count() : 0))")
    @Mapping(target = "tauxOccupation", expression = "java(entity.getCapacite() > 0 ? (double) (entity.getEmplacements() != null ? entity.getEmplacements().stream().filter(e -> Boolean.TRUE.equals(e.getOccupe())).count() : 0) / entity.getCapacite() * 100 : 0.0)")
    ChambreFroideResponseDTO toResponseDTO(ChambreFroide entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "emplacements", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ChambreFroide toEntity(ChambreFroideRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "emplacements", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(ChambreFroideRequestDTO requestDTO, @MappingTarget ChambreFroide entity);
}
