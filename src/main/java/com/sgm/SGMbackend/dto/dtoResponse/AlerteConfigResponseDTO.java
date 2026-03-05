package com.sgm.SGMbackend.dto.dtoResponse;

import com.sgm.SGMbackend.entity.enums.TypeAlerte;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlerteConfigResponseDTO {
    private Long id;
    private TypeAlerte type;
    private Float seuil;
    private String canal;
    private String destinataires;
}
