package com.sgm.SGMbackend.dto.dtoResponse;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmplacementResponseDTO {

    private Long id;
    private String code; // ex: CF-01-E1
    private Boolean occupe;
    private LocalDateTime dateAffectation;

    // Chambre froide parente
    private Long chambreFroideId;
    private String chambreFroideNumero;

    // Dépouille occupant l'emplacement (si occupe = true)
    private Long depouilleId;
    private String identifiantUniqueDepouille;
    private String nomDepouille;
    private String prenomDepouille;

    private LocalDateTime createdAt;
}
