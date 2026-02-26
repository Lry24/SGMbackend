package com.sgm.SGMbackend.entity;

import com.sgm.SGMbackend.entity.enums.TypeAlerte;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alertes_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlerteConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private TypeAlerte type;

    private Float seuil;

    private String canal; // ex: MAIL, APP

    private String destinataires; // emails séparés par virgules ou rôles
}
