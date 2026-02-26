package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.dto.dtoRequest.FactureRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.FactureResponseDTO;
import com.sgm.SGMbackend.dto.dtoResponse.PaiementResponseDTO;
import com.sgm.SGMbackend.entity.enums.StatutFacture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FactureService {

    Page<FactureResponseDTO> findAll(StatutFacture statut, Long familleId, String dateDebut, String dateFin,
            Pageable pageable);

    FactureResponseDTO findById(Long id);

    FactureResponseDTO getByDepouilleId(Long id);

    FactureResponseDTO calculer(Long depouillId);

    FactureResponseDTO creer(FactureRequestDTO dto);

    FactureResponseDTO modifier(Long id, FactureRequestDTO dto);

    FactureResponseDTO emettre(Long id);

    PaiementResponseDTO enregistrerPaiement(Long id, Double montant, String mode, String reference);

    List<PaiementResponseDTO> getPaiements(Long id);

    FactureResponseDTO annuler(Long id, String motif);

    byte[] genererPdf(Long id);
}
