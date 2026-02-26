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
public class PaiementResponseDTO {
    private Long id;
    private Double montant;
    private String mode;
    private String reference;
    private LocalDateTime createdAt;
}
