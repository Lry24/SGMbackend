package com.sgm.SGMbackend.dto.dtoResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MouvementCaisseResponseDTO {
    private Long id;
    private LocalDateTime date;
    private Double montant;
    private String type;
    private String modePaiement;
    private String libelle;
    private Long factureId;
    private String factureNumero;
    private String familleNom;
    private String defuntNom;
    private LocalDateTime createdAt;
}
