package com.sgm.SGMbackend.dto.dtoRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BaremeRequestDTO {
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotNull(message = "Le prix est obligatoire")
    private Double prix;

    private String unite;
    private Boolean actif;
}
