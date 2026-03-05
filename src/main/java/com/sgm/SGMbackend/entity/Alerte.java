package com.sgm.SGMbackend.entity;

import com.sgm.SGMbackend.entity.enums.TypeAlerte;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "alertes")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alerte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeAlerte type;

    @Column(nullable = false)
    private String message;

    private String roleDestinataire;

    @Builder.Default
    private Boolean acquittee = false;

    private LocalDateTime dateAcquittement;
    private String commentaireAcquittement;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime dateCreation;
}
