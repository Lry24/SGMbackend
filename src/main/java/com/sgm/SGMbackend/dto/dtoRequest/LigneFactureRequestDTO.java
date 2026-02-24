package com.sgm.SGMbackend.dto.dtoRequest;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneFactureRequestDTO {

    @NotBlank(message = "La prestation est obligatoire")
    private String prestation;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantite;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @Positive(message = "Le prix unitaire doit être positif")
    private Double prixUnitaire;
}