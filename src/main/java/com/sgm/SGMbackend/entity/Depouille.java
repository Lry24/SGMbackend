package com.sgm.SGMbackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entité centrale du système : représente un défunt pris en charge par la
 * morgue.
 * Identifiant métier unique format : SGM-2026-00001
 * Workflow statut : RECUE → EN_CHAMBRE_FROIDE → EN_AUTOPSIE → PREPAREE →
 * RESTITUEE
 */
@Entity
@Table(name = "depouilles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString(exclude = { "emplacement", "famille", "documents" })
@EqualsAndHashCode(exclude = { "emplacement", "famille", "documents" })
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Depouille {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identifiant métier unique : SGM-2026-00001
    @Column(unique = true, nullable = false)
    private String identifiantUnique;

    private String nomDefunt;
    private String prenomDefunt;
    private LocalDate dateNaissance;

    @Column(nullable = false)
    private LocalDateTime dateDeces;

    @Column(nullable = false)
    private LocalDateTime dateArrivee;

    private String causePresumee;
    private String provenance; // hôpital, domicile, accident...
    private String observations;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutDepouille statut = StatutDepouille.RECUE;

    // Lien vers l'emplacement (chambre froide) — géré par DEV C
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emplacement_id")
    private Emplacement emplacement;

    // Lien vers la famille — géré par DEV D
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "famille_id")
    private Famille famille;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "entiteId") // On utilise le champ générique pour le lien
    private List<Document> documents;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
