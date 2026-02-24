package com.sgm.SGMbackend.dto.dtoRequest;

import com.sgm.SGMbackend.entity.enums.StatutChambre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChambreFroideRequestDTO {

    @NotBlank(message = "Le numéro de chambre est obligatoire")
    private String numero; // ex: CF-01

    @NotNull(message = "La capacité est obligatoire")
    @Positive(message = "La capacité doit être positive")
    private Integer capacite;

    private Float temperatureCible; // Température idéale en °C

    @NotNull(message = "Le statut est obligatoire")
    private StatutChambre statut; // OPERATIONNELLE, MAINTENANCE, HORS_SERVICE
}
