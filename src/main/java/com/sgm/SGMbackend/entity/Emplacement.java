package com.sgm.SGMbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "emplacements")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Emplacement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code; // ex: CF-01-E1

    @Builder.Default
    private Boolean occupe = false;

    // --- RELATION MANQUANTE AJOUTÉE ICI ---
    @ManyToOne
    @JoinColumn(name = "chambre_froide_id")
    private ChambreFroide chambreFroide;
    // Ce nom "chambreFroide" doit être identique au mappedBy de la classe ChambreFroide
    // ---------------------------------------

    @OneToOne(mappedBy = "emplacement")
    private Depouille depouille; // Relation vers l'entité de DEV B

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}