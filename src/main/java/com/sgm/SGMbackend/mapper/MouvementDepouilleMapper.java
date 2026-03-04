package com.sgm.SGMbackend.mapper;

import com.sgm.SGMbackend.dto.dtoResponse.MouvementDepouilleResponseDTO;
import com.sgm.SGMbackend.entity.MouvementDepouille;
import org.springframework.stereotype.Component;

@Component
public class MouvementDepouilleMapper {

    public MouvementDepouilleResponseDTO toResponseDTO(MouvementDepouille entity) {
        if (entity == null)
            return null;
        return MouvementDepouilleResponseDTO.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .dateMouvement(entity.getDateMouvement())
                .statut(entity.getStatut())
                .emplacementCode(entity.getEmplacement() != null ? entity.getEmplacement().getCode() : null)
                .build();
    }
}
