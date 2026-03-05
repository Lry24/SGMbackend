package com.sgm.SGMbackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sgm.SGMbackend.entity.enums.StatutFacture;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "factures")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String numero; // FAC-2026-0001

    private LocalDateTime dateEmission; // Ajouté pour conformité diagramme

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "famille_id", nullable = false)
    private Famille famille;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "depouille_id")
    private Depouille depouille;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autopsie_id")
    private Autopsie autopsie;

    @Builder.Default
    private Double montantTotal = 0.0;
    @Builder.Default
    private Double montantPaye = 0.0;
    @Builder.Default
    private Double remise = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutFacture statut = StatutFacture.BROUILLON;

    @JsonIgnore
    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LigneFacture> lignes = new ArrayList<>();

    private String motifAnnulation;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
