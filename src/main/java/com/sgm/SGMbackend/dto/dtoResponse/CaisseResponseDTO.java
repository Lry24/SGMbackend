package com.sgm.SGMbackend.dto.dtoResponse;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CaisseResponseDTO {
    private Long id;
    private LocalDateTime dateOuverture;
    private LocalDateTime dateFermeture;
    private Double fondCaisse;
    private Double soldeFinal;
    private String statut;
    private LocalDateTime createdAt;
}
