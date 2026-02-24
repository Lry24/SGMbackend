package com.sgm.SGMbackend.entity;

import com.sgm.SGMbackend.entity.enums.StatutAutopsie;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/**
 * Entité Autopsie — liée à une Depouille (Many-to-One).
 * Règle métier clé : une seule autopsie active (PLANIFIEE ou EN_COURS) par
 * dépouille.
 * Workflow : PLANIFIEE → EN_COURS → TERMINEE → RAPPORT_VALIDE
 */
@Entity
@Table(name = "autopsies")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Autopsie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depouille_id", nullable = false)
    private Depouille depouille;

    // UUID de l'utilisateur médecin légiste (référence vers Utilisateur Supabase)
    @Column(nullable = false)
    private String medecinId;

    private String nomMedecin; // copie dénormalisée pour les rapports

    @Column(nullable = false)
    private LocalDateTime datePlanifiee;

    private String salle; // Ajouté pour conformité diagramme
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    @Column(columnDefinition = "TEXT")
    private String rapport;

    @Column(columnDefinition = "TEXT")
    private String conclusion;

    @Column(columnDefinition = "TEXT")
    private String analysesComplementaires;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutAutopsie statut = StatutAutopsie.PLANIFIEE;

    @OneToOne(mappedBy = "autopsie")
    private Facture facture;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
