package com.sgm.SGMbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chambres_froides")
@EntityListeners(AuditingEntityListener.class)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ChambreFroide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String numero;          // ex: CF-01, CF-02

    @Column(nullable = false)
    private Integer capacite;       // Nombre total d'emplacements

    private Float temperatureCible;  // Température idéale en °C
    private Float temperatureActuelle;
    private String statut;           // OPERATIONNELLE, MAINTENANCE, HORS_SERVICE

    @OneToMany(mappedBy = "chambreFroide", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Emplacement> emplacements;

    @CreatedDate @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
