package com.sgm.SGMbackend.dto.dtoRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestitutionRequestDTO {

    @NotNull(message = "L'identifiant de la dépouille est obligatoire")
    private Long depouilleId;

    @NotNull(message = "L'identifiant de la famille est obligatoire")
    private Long familleId;

    @NotNull(message = "La date planifiée est obligatoire")
    private LocalDateTime datePlanifiee;

    @NotBlank(message = "Le nom du représentant de la famille est obligatoire")
    private String representantFamille;

    @NotBlank(message = "La référence de pièce d'identité est obligatoire")
    private String pieceIdentiteRef;

    private String numeroBonRestitution;
}
