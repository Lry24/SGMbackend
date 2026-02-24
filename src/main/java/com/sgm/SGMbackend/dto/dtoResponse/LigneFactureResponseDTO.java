package com.sgm.SGMbackend.dto.dtoResponse;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneFactureResponseDTO {

    private Long id;
    private String prestation;
    private Integer quantite;
    private Double prixUnitaire;
    private Double montantLigne;    // quantite * prixUnitaire
}