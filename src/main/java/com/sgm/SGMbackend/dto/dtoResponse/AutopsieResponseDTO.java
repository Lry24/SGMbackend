package com.sgm.SGMbackend.dto.dtoResponse;

import com.sgm.SGMbackend.entity.enums.StatutAutopsie;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutopsieResponseDTO {

    private Long id;

    // Dépouille concernée
    private Long depouilleId;
    private String identifiantUniqueDepouille;
    private String nomDefunt;

    // Médecin
    private String medecinId;
    private String nomMedecin;

    private LocalDateTime datePlanifiee;
    private String salle;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private StatutAutopsie statut;

    private String rapport;
    private String conclusion;
    private String analysesComplementaires;

    private LocalDateTime createdAt;
}
