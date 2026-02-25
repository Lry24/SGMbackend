package com.sgm.SGMbackend.mapper;

import com.sgm.SGMbackend.dto.*;
import com.sgm.SGMbackend.entity.Depouille;
public class DepouilleMapperImpl {


    public static Depouille toEntity(DepouilleRequestDTO dto) {

        return Depouille.builder()
                .nomDefunt(dto.getNomDefunt())
                .prenomDefunt(dto.getPrenomDefunt())
                .dateDeces(dto.getDateDeces())
                .causePresumee(dto.getCausePresumee())
                .provenance(dto.getProvenance())
                .build();
    }

    public static DepouilleResponseDTO toDTO(Depouille entity) {

        return DepouilleResponseDTO.builder()
                .id(entity.getId())
                .identifiantUnique(entity.getIdentifiantUnique())
                .nomDefunt(entity.getNomDefunt())
                .prenomDefunt(entity.getPrenomDefunt())
                .statut(entity.getStatut())
                .dateArrivee(entity.getDateArrivee())
                .build();
    }
}
