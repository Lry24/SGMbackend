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

    private String prestation;      // Libellé de la prestation
    private Integer quantite = 1;
    private Double prixUnitaire;

    // Calculé : quantite * prixUnitaire
    public Double getMontantLigne() {
        return quantite * prixUnitaire;
    }
}