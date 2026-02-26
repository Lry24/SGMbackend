package com.sgm.SGMbackend.dto.dtoResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaisseResponseDTO {
    private Long id;
    private LocalDateTime dateOuverture;
    private LocalDateTime dateFermeture;
    private Double fondCaisse;
    private Double soldeFinal;
    private Double totalEncaissements;
    private Double ecart;
    private Boolean estFermee;
}
