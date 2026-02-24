package com.sgm.SGMbackend.dto.dtoResponse;

import com.sgm.SGMbackend.entity.enums.StatutChambre;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChambreFroideResponseDTO {

    private Long id;
    private String numero; // ex: CF-01
    private Integer capacite;
    private Integer nombreOccupes; // Calculé : emplacements occupés
    private Integer nombreDisponibles; // capacite - nombreOccupes
    private Double tauxOccupation; // En %
    private Float temperatureCible;
    private Float temperatureActuelle;
    private StatutChambre statut; // OPERATIONNELLE, MAINTENANCE, HORS_SERVICE
    private List<EmplacementResponseDTO> emplacements;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
