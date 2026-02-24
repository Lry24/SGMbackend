package com.sgm.SGMbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/**
 * Stub de l'entité Emplacement (chambre froide) — sera complétée par DEV C.
 * Déclarée ici pour permettre la compilation de Depouille.
 */
@Entity
@Table(name = "emplacements")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Emplacement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code; // ex: CF-A-01
    private String description;
    private Boolean occupe = false;
    private Integer capacite;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
