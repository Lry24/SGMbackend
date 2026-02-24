package com.sgm.SGMbackend.mapper;

import com.sgm.SGMbackend.dto.dtoRequest.DepouilleRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.DepouilleResponseDTO;
import com.sgm.SGMbackend.entity.Depouille;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = { DocumentMapper.class })
public interface DepouilleMapper {

    @Mapping(target = "emplacementId", source = "emplacement.id")
    @Mapping(target = "emplacementCode", source = "emplacement.code")
    @Mapping(target = "familleId", source = "famille.id")
    @Mapping(target = "nomContactFamille", source = "famille.tuteurLegal")
    @Mapping(target = "documents", source = "documents")
    DepouilleResponseDTO toResponseDTO(Depouille entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "identifiantUnique", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "emplacement", ignore = true)
    @Mapping(target = "famille", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Depouille toEntity(DepouilleRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "identifiantUnique", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "emplacement", ignore = true)
    @Mapping(target = "famille", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(DepouilleRequestDTO requestDTO, @MappingTarget Depouille entity);
}
