package com.sgm.SGMbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@EntityListeners(AuditingEntityListener.class)
@Data @NoArgsConstructor @AllArgsConstructor @Builder

public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomFichier;

    @Column(nullable = false)
    private String typeDocument;  // CERTIFICAT_DECES, ACTE_NAISSANCE, PIECE_IDENTITE...

    @Column(nullable = false)
    private String cheminStorage; // Chemin dans Supabase Storage

    private Long tailleOctets;
    private String mimeType;

    // Entité liée (polymorphique simple)
    private String entiteType;   // DEPOUILLE, FAMILLE, AUTOPSIE
    private Long entiteId;

    @CreatedDate @Column(updatable = false)
    private LocalDateTime createdAt;
}
