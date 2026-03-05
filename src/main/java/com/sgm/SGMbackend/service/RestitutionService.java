package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.dto.dtoRequest.RestitutionRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.RestitutionResponseDTO;
import com.sgm.SGMbackend.entity.enums.StatutRestitution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface RestitutionService {
    RestitutionResponseDTO planifier(RestitutionRequestDTO requestDTO);

    RestitutionResponseDTO confirmer(Long id);

    void annuler(Long id, String motif);

    RestitutionResponseDTO effectuer(Long id, String pieceIdentiteRef);

    RestitutionResponseDTO findById(Long id);

    Page<RestitutionResponseDTO> findAll(StatutRestitution statut, Pageable pageable);

    List<RestitutionResponseDTO> getPlanning(LocalDateTime date);

    com.sgm.SGMbackend.dto.dtoResponse.FactureResponseDTO getFactureByDepouille(Long depouilleId);

    byte[] genererAttestationRestitution(Long id);
}
