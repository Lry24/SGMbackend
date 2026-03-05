package com.sgm.SGMbackend.entity;

import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mouvements_depouille")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MouvementDepouille {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depouille_id", nullable = false)
    private Depouille depouille;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime dateMouvement;

    @Enumerated(EnumType.STRING)
    private StatutDepouille statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emplacement_id")
    private Emplacement emplacement;
}
