package com.sgm.SGMbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/**
 * Stub de l'entité Famille — sera complétée par DEV D.
 * Déclarée ici pour permettre la compilation des entités Depouille et
 * Restitution.
 */
@Entity
@Table(name = "familles")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Famille {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomContact;
    private String prenomContact;
    private String telephone;
    private String email;
    private String adresse;
    private String lienParente; // Père, mère, époux/se, etc.

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
