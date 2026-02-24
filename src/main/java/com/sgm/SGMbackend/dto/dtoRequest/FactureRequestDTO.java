package com.sgm.SGMbackend.dto.dtoRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactureRequestDTO {

    @NotNull(message = "L'identifiant de la dépouille est obligatoire")
    private Long depouilleId;

    @NotNull(message = "L'identifiant de la famille est obligatoire")
    private Long familleId;

    @NotNull(message = "Les lignes de facturation sont obligatoires")
    @Valid
    private List<LigneFactureRequestDTO> lignes;

    private Long autopsieId; // La facture peut être générée par une autopsie
    private LocalDateTime dateEmission;
    @Builder.Default
    private Double remise = 0.0;
}
