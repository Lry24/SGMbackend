package com.sgm.SGMbackend.mapper;

import com.sgm.SGMbackend.dto.dtoRequest.FactureRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.FactureResponseDTO;
import com.sgm.SGMbackend.entity.Facture;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = { FamilleMapper.class, LigneFactureMapper.class })
public interface FactureMapper {

    @Mapping(target = "depouilleId", source = "depouille.id")
    @Mapping(target = "autopsieId", source = "autopsie.id")
    FactureResponseDTO toResponseDTO(Facture entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numero", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "montantTotal", ignore = true)
    @Mapping(target = "montantPaye", ignore = true)
    @Mapping(target = "famille", ignore = true)
    @Mapping(target = "depouille", ignore = true)
    @Mapping(target = "autopsie", ignore = true)
    @Mapping(target = "motifAnnulation", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lignes", ignore = true)
    Facture toEntity(FactureRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numero", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "montantTotal", ignore = true)
    @Mapping(target = "montantPaye", ignore = true)
    @Mapping(target = "famille", ignore = true)
    @Mapping(target = "depouille", ignore = true)
    @Mapping(target = "autopsie", ignore = true)
    @Mapping(target = "motifAnnulation", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lignes", ignore = true)
    void updateEntity(FactureRequestDTO requestDTO, @MappingTarget Facture entity);
}
