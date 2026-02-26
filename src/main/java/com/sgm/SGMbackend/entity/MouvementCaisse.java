package com.sgm.SGMbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "mouvements_caisse")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MouvementCaisse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private Double montant;

    @Column(nullable = false)
    private String type; // ENCAISSEMENT, DECAISSEMENT

    private String modePaiement; // ESPECES, CHEQUE, VIREMENT, CARTE

    private String libelle; // ex: Paiement Facture FAC-...

    @ManyToOne
    @JoinColumn(name = "facture_id")
    private Facture facture;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
