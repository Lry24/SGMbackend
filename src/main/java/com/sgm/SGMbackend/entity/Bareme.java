package com.sgm.SGMbackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "baremes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bareme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    @Column(nullable = false)
    private Double prix;

    private String unite; // Ex: Forfait, Heure, Km, etc.

    private String description;

    @Builder.Default
    private Boolean actif = true;
}
