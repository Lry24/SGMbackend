package com.sgm.SGMbackend.dto.dtoResponse;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilleResponseDTO {

    private Long id;
    private String tuteurLegal;
    private String lienParente;
    private String telephone;
    private String email;
    private String adresse;
    private String pieceIdentiteRef;
    private Boolean actif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}