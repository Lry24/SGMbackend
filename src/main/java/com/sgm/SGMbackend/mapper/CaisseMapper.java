package com.sgm.SGMbackend.mapper;

import com.sgm.SGMbackend.dto.dtoResponse.CaisseResponseDTO;
import com.sgm.SGMbackend.entity.Caisse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CaisseMapper {
    CaisseResponseDTO toResponseDTO(Caisse entity);
}
