package com.sgm.SGMbackend.dto.dtoRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepouilleRequestDTO {

    private String nomDefunt;
    private String prenomDefunt;
    private LocalDate dateNaissance;

    @NotNull(message = "La date de décès est obligatoire")
    private LocalDateTime dateDeces;

    @NotNull(message = "La date d'arrivée est obligatoire")
    private LocalDateTime dateArrivee;

    private String causePresumee;

    @NotBlank(message = "La provenance est obligatoire")
    private String provenance; // hôpital, domicile, accident...

    private String observations;

    private Long emplacementId; // optionnel à la création
    private Long familleId; // optionnel à la création
}
