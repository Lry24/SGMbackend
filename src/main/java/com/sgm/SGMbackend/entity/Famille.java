package com.sgm.SGMbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private String tuteurLegal;       // Nom complet du représentant légal

    @Column(nullable = false)
    private String lienParente;       // Fils, fille, époux(se), frère, sœur...

    @Column(nullable = false)
    private String telephone;

    private String email;

    private String adresse;

    private String pieceIdentiteRef;

    @Column(nullable = false)
    private Boolean actif = true;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}