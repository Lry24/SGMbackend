package com.sgm.SGMbackend.entity;


import com.sgm.SGMbackend.entity.enums.StatutFacture;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
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
    private String numero;         // FAC-2026-0001

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "famille_id", nullable = false)
    private Famille famille;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "depouille_id")
    private Depouille depouille;

    private Double montantTotal = 0.0;
    private Double montantPaye  = 0.0;
    private Double remise       = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutFacture statut = StatutFacture.BROUILLON;

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LigneFacture> lignes;

    private String motifAnnulation;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}