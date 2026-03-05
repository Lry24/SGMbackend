package com.sgm.SGMbackend.dto.dtoResponse;

import com.sgm.SGMbackend.entity.enums.TypeAlerte;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlerteResponseDTO {
    private Long id;
    private TypeAlerte type;
    private String message;
    private String roleDestinataire;
    private Boolean acquittee;
    private LocalDateTime dateCreation;
    private LocalDateTime dateAcquittement;
    private String commentaireAcquittement;
}
