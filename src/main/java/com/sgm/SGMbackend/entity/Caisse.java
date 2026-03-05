package com.sgm.SGMbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "caisses")
@EntityListeners(AuditingEntityListener.class)
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
    private Double fondCaisse; // Montant initial le matin

    private Double soldeFinal; // Saisi à la fermeture

    @Builder.Default
    private String statut = "OUVERTE"; // OUVERTE, FERMEE

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
