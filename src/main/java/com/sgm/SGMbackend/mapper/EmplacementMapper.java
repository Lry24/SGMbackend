package com.sgm.SGMbackend.mapper;

import com.sgm.SGMbackend.dto.dtoRequest.EmplacementRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.EmplacementResponseDTO;
import com.sgm.SGMbackend.entity.Emplacement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmplacementMapper {

    @Mapping(target = "chambreFroideId", source = "chambreFroide.id")
    @Mapping(target = "chambreFroideNumero", source = "chambreFroide.numero")
    @Mapping(target = "depouilleId", source = "depouille.id")
    @Mapping(target = "identifiantUniqueDepouille", source = "depouille.identifiantUnique")
    EmplacementResponseDTO toResponseDTO(Emplacement entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chambreFroide", ignore = true)
    @Mapping(target = "depouille", ignore = true)
    @Mapping(target = "occupe", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "dateAffectation", ignore = true)
    Emplacement toEntity(EmplacementRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chambreFroide", ignore = true)
    @Mapping(target = "depouille", ignore = true)
    @Mapping(target = "occupe", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "dateAffectation", ignore = true)
    void updateEntity(EmplacementRequestDTO requestDTO, @MappingTarget Emplacement entity);
}
