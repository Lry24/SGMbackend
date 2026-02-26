package com.sgm.SGMbackend.dto.dtoRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaremeRequestDTO {

    @NotBlank(message = "Le nom de la prestation est obligatoire")
    private String nom;

    @NotNull(message = "Le prix de la prestation est obligatoire")
    private Double prix;

    private String unite;

    private String description;
}
