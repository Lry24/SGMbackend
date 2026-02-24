package com.sgm.SGMbackend.mapper;

import com.sgm.SGMbackend.dto.dtoRequest.RestitutionRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.RestitutionResponseDTO;
import com.sgm.SGMbackend.entity.Restitution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RestitutionMapper {

    @Mapping(target = "depouilleId", source = "depouille.id")
    @Mapping(target = "identifiantUniqueDepouille", source = "depouille.identifiantUnique")
    @Mapping(target = "nomDefunt", source = "depouille.nomDefunt")
    @Mapping(target = "familleId", source = "famille.id")
    @Mapping(target = "nomContactFamille", source = "famille.tuteurLegal")
    @Mapping(target = "valideeParId", source = "valideePar.id")
    @Mapping(target = "valideeParNom", source = "valideePar.nomComplet")
    RestitutionResponseDTO toResponseDTO(Restitution entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "depouille", ignore = true)
    @Mapping(target = "famille", ignore = true)
    @Mapping(target = "dateEffective", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "motifAnnulation", ignore = true)
    @Mapping(target = "facturesSoldees", ignore = true)
    @Mapping(target = "documentsComplets", ignore = true)
    @Mapping(target = "valideePar", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Restitution toEntity(RestitutionRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "depouille", ignore = true)
    @Mapping(target = "famille", ignore = true)
    @Mapping(target = "dateEffective", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "motifAnnulation", ignore = true)
    @Mapping(target = "facturesSoldees", ignore = true)
    @Mapping(target = "documentsComplets", ignore = true)
    @Mapping(target = "valideePar", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(RestitutionRequestDTO requestDTO, @MappingTarget Restitution entity);
}
