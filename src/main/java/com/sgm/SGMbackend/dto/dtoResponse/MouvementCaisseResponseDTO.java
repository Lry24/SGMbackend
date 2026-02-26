package com.sgm.SGMbackend.dto.dtoResponse;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MouvementCaisseResponseDTO {
    private Long id;
    private LocalDateTime date;
    private Double montant;
    private String type;
    private String modePaiement;
    private String libelle;
    private Long factureId;
    private String factureNumero;
    private LocalDateTime createdAt;
}
