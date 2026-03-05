package com.sgm.SGMbackend.dto.dtoResponse;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BaremeResponseDTO {
    private Long id;
    private String nom;
    private Double prix;
    private String unite;
    private Boolean actif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
