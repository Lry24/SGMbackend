package com.sgm.SGMbackend.dto.dtoResponse;

import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepouilleResponseDTO {

    private Long id;
    private String identifiantUnique; // ex: SGM-2026-00001
    private String nomDefunt;
    private String prenomDefunt;
    private LocalDate dateNaissance;
    private LocalDateTime dateDeces;
    private LocalDateTime dateArrivee;
    private String causePresumee;
    private String provenance;
    private String observations;
    private StatutDepouille statut;
    private String statutPaiement; // ex: EN_ATTENTE, PAYEE, PARTIELLEMENT_PAYEE

    // Infos emplacement (sans charger toute la chambre)
    private Long emplacementId;
    private String emplacementCode; // ex: CF-01-E1

    // Infos famille (résumé)
    private Long familleId;
    private String nomContactFamille;

    private List<DocumentResponseDTO> documents;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
