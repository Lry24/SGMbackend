package com.sgm.SGMbackend.dto.dtoRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutopsieRequestDTO {

    @NotNull(message = "L'identifiant de la dépouille est obligatoire")
    private Long depouilleId;

    @NotBlank(message = "L'identifiant du médecin est obligatoire")
    private String medecinId;

    @NotBlank(message = "Le nom du médecin est obligatoire")
    private String nomMedecin;

    @NotNull(message = "La date planifiée est obligatoire")
    private LocalDateTime datePlanifiee;

    private String salle;

    private String rapport;
    private String conclusion;
    private String analysesComplementaires;
}
