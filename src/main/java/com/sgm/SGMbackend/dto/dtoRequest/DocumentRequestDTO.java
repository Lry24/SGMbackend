package com.sgm.SGMbackend.dto.dtoRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRequestDTO {

    @NotBlank(message = "Le nom du fichier est obligatoire")
    private String nomFichier;

    @NotBlank(message = "Le type de document est obligatoire")
    private String typeDocument; // CERTIFICAT_DECES, ACTE_NAISSANCE, PIECE_IDENTITE...

    @NotBlank(message = "Le chemin Supabase Storage est obligatoire")
    private String cheminStorage;

    private Long tailleOctets;
    private String mimeType;

    @NotBlank(message = "Le type d'entité liée est obligatoire")
    private String entiteType; // DEPOUILLE, FAMILLE, AUTOPSIE

    @NotNull(message = "L'identifiant de l'entité liée est obligatoire")
    private Long entiteId;
}
