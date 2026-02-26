package com.sgm.SGMbackend.dto.dtoResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaremeResponseDTO {
    private Long id;
    private String nom;
    private Double prix;
    private String unite;
    private String description;
    private Boolean actif;
}
