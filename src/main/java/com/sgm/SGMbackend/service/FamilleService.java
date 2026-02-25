package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.dto.dtoRequest.FamilleRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.FamilleResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FamilleService {
    Page<FamilleResponseDTO> findAll(Pageable pageable);

    FamilleResponseDTO findById(Long id);

    FamilleResponseDTO create(FamilleRequestDTO dto);

    FamilleResponseDTO update(Long id, FamilleRequestDTO dto);

    void delete(Long id);

    Page<FamilleResponseDTO> recherche(String q, Pageable pageable);

    void lierDepouille(Long familleId, Long depouilleId);

    java.util.List<com.sgm.SGMbackend.dto.dtoResponse.DepouilleResponseDTO> getDepouillesByFamille(Long id);
}
