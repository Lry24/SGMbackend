package com.sgm.SGMbackend.mapper;

import com.sgm.SGMbackend.dto.dtoRequest.AutopsieRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.AutopsieResponseDTO;
import com.sgm.SGMbackend.entity.Autopsie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AutopsieMapper {

    @Mapping(target = "depouilleId", source = "depouille.id")
    @Mapping(target = "identifiantUniqueDepouille", source = "depouille.identifiantUnique")
    @Mapping(target = "nomDefunt", source = "depouille.nomDefunt")
    AutopsieResponseDTO toResponseDTO(Autopsie entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "depouille", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "dateDebut", ignore = true)
    @Mapping(target = "dateFin", ignore = true)
    @Mapping(target = "rapport", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Autopsie toEntity(AutopsieRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "depouille", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "dateDebut", ignore = true)
    @Mapping(target = "dateFin", ignore = true)
    @Mapping(target = "rapport", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(AutopsieRequestDTO requestDTO, @MappingTarget Autopsie entity);
}
