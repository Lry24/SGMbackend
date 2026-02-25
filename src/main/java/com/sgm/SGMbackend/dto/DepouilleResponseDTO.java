package com.sgm.SGMbackend.dto;

import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DepouilleResponseDTO {

    private Long id;
    private String identifiantUnique;
    private String nomDefunt;
    private String prenomDefunt;
    private StatutDepouille statut;
    private LocalDateTime dateArrivee;
}