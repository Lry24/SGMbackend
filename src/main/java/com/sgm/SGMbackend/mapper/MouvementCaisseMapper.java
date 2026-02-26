package com.sgm.SGMbackend.mapper;

import com.sgm.SGMbackend.dto.dtoResponse.MouvementCaisseResponseDTO;
import com.sgm.SGMbackend.entity.MouvementCaisse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MouvementCaisseMapper {
    @Mapping(target = "factureId", source = "facture.id")
    @Mapping(target = "factureNumero", source = "facture.numero")
    MouvementCaisseResponseDTO toResponseDTO(MouvementCaisse entity);
}
