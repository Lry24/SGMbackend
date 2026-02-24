package com.sgm.SGMbackend.dto.dtoRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmplacementRequestDTO {

    @NotBlank(message = "Le code de l'emplacement est obligatoire")
    private String code; // ex: CF-01-E1

    @NotNull(message = "L'identifiant de la chambre froide est obligatoire")
    private Long chambreFroideId;
}
