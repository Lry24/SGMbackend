package com.sgm.SGMbackend.dto.dtoResponse;

import com.sgm.SGMbackend.entity.enums.StatutFacture;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactureResponseDTO {

    private Long id;
    private String numero; // FAC-2026-0001
    private LocalDateTime dateEmission;
    private FamilleResponseDTO famille;
    private Long depouilleId;
    private String nomDefunt;
    private String prenomDefunt;
    private Long autopsieId;
    private Double montantTotal;
    private Double montantPaye;
    private Double remise;
    private StatutFacture statut;
    private List<LigneFactureResponseDTO> lignes;
    private String motifAnnulation;
    private LocalDateTime createdAt;
}