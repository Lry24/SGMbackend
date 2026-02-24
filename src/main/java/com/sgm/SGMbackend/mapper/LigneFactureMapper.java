package com.sgm.SGMbackend.mapper;

import com.sgm.SGMbackend.dto.dtoRequest.LigneFactureRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.LigneFactureResponseDTO;
import com.sgm.SGMbackend.entity.LigneFacture;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LigneFactureMapper {

    @Mapping(target = "montantLigne", expression = "java(entity.getMontantLigne())")
    LigneFactureResponseDTO toResponseDTO(LigneFacture entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "facture", ignore = true)
    LigneFacture toEntity(LigneFactureRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "facture", ignore = true)
    void updateEntity(LigneFactureRequestDTO requestDTO, @MappingTarget LigneFacture entity);
}
