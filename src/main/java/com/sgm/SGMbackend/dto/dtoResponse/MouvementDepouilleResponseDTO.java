package com.sgm.SGMbackend.dto.dtoResponse;

import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MouvementDepouilleResponseDTO {
    private Long id;
    private String description;
    private LocalDateTime dateMouvement;
    private StatutDepouille statut;
    private String emplacementCode;
}
