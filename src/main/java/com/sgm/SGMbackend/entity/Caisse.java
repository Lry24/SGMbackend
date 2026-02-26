package com.sgm.SGMbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "caisses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Caisse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateOuverture;

    private LocalDateTime dateFermeture;

    @Column(nullable = false)
    private Double fondCaisse;

    private Double soldeFinal; // Déclaré lors de la fermeture

    @Builder.Default
    private Double totalEncaissements = 0.0; // Calculé à partir des paiements du jour

    private Double ecart; // soldeFinal - (fondCaisse + totalEncaissements)

    @Builder.Default
    private Boolean estFermee = false;
}
