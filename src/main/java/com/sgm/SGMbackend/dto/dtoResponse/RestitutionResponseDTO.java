package com.sgm.SGMbackend.dto.dtoResponse;

import com.sgm.SGMbackend.entity.enums.StatutRestitution;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestitutionResponseDTO {

    private Long id;

    // Dépouille
    private Long depouilleId;
    private String identifiantUniqueDepouille;
    private String nomDefunt;

    // Famille
    private Long familleId;
    private String nomContactFamille;

    private LocalDateTime datePlanifiee;
    private LocalDateTime dateEffective;
    private StatutRestitution statut;

    private String representantFamille;
    private String pieceIdentiteRef;
    private String numeroBonRestitution;
    private String motifAnnulation;

    // Checklist pré-requis
    private Boolean facturesSoldees;
    private Boolean documentsComplets;

    // Validation
    private String valideeParId;
    private String valideeParNom;

    private LocalDateTime createdAt;
}
