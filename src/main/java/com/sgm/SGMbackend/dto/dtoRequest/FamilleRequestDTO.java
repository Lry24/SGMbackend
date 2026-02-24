package com.sgm.SGMbackend.dto.dtoRequest;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilleRequestDTO {

    @NotBlank(message = "Le tuteur légal est obligatoire")
    private String tuteurLegal;

    @NotBlank(message = "Le lien de parenté est obligatoire")
    private String lienParente;

    @NotBlank(message = "Le téléphone est obligatoire")
    private String telephone;

    private String email;

    private String adresse;

    private String pieceIdentiteRef;
}
