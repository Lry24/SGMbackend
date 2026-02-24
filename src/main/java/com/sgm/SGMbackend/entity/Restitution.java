package com.sgm.SGMbackend.entity;

import com.sgm.SGMbackend.entity.enums.StatutRestitution;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/**
 * Entité Restitution — remise d'une dépouille à sa famille.
 * Règle métier : confirmation possible seulement si :
 * 1. Facture entièrement soldée
 * 2. Famille identifiée (déjà liée)
 * 3. Dépouille dans un statut compatible (PREPAREE ou EN_CHAMBRE_FROIDE)
 */
@Entity
@Table(name = "restitutions")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restitution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "depouille_id", nullable = false)
    private Depouille depouille;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "famille_id", nullable = false)
    private Famille famille;

    @Column(nullable = false)
    private LocalDateTime datePlanifiee;

    private LocalDateTime dateEffective;
    private String motifAnnulation;
    private String numeroBonRestitution; // Ajouté pour conformité diagramme
    private String representantFamille; // nom de la personne qui récupère la dépouille
    private String pieceIdentiteRef; // référence pièce d'identité du représentant

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutRestitution statut = StatutRestitution.PLANIFIEE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validateur_id")
    private Utilisateur valideePar;

    // Checklist pré-requis (calculés au moment de la confirmation)
    @Builder.Default
    private Boolean facturesSoldees = false;
    @Builder.Default
    private Boolean documentsComplets = false;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
