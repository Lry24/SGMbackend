package com.sgm.SGMbackend.dto.dtoRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffecterEmplacementDTO {
    private Long depouilleId;
    private Long emplacementId;
    private LocalDateTime dateAffectation;
}
