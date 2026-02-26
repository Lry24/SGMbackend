package com.sgm.SGMbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "paiements")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id", nullable = false)
    private Facture facture;

    @Column(nullable = false)
    private Double montant;

    @Column(nullable = false)
    private String mode; // Ex: ESPECES, CARTE, CHEQUE, VIREMENT

    private String reference; // Ref transaction, numero cheque, etc.

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
