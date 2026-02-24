package com.sgm.SGMbackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lignes_facture")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneFacture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id", nullable = false)
    private Facture facture;

    private String prestation; // Libellé de la prestation
    @Builder.Default
    private Integer quantite = 1;

    @Column(nullable = false)
    private Double prixUnitaire;

    // Calculé : quantite * prixUnitaire (null-safe)
    public Double getMontantLigne() {
        if (prixUnitaire == null || quantite == null)
            return 0.0;
        return quantite * prixUnitaire;
    }
}