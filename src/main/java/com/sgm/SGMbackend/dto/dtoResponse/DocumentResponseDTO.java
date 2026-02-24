package com.sgm.SGMbackend.dto.dtoResponse;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponseDTO {

    private Long id;
    private String nomFichier;
    private String typeDocument; // CERTIFICAT_DECES, ACTE_NAISSANCE, PIECE_IDENTITE...
    private String cheminStorage; // Chemin dans Supabase Storage
    private Long tailleOctets;
    private String mimeType;

    // Entité liée
    private String entiteType; // DEPOUILLE, FAMILLE, AUTOPSIE
    private Long entiteId;

    private LocalDateTime createdAt;
}
