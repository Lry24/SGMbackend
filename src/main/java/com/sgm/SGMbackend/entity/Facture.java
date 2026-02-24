package com.sgm.SGMbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Stub de l'entité Facture — sera complétée par DEV D (module Facturation).
 * Déclarée ici pour que FactureRepository puisse être injecté dans
 * RestitutionService.
 */
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depouille_id")
    private Depouille depouille;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.sgm.SGMbackend.entity.enums.StatutFacture statut = com.sgm.SGMbackend.entity.enums.StatutFacture.EN_ATTENTE;

    private Double montantTotal;
    private Double montantPaye;
    private String reference;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
