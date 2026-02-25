package com.sgm.SGMbackend.entity;

import com.sgm.SGMbackend.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "utilisateurs")
@EntityListeners(AuditingEntityListener.class)

@Getter
@Setter
@ToString(exclude = "restitutionsValidees")
@EqualsAndHashCode(exclude = "restitutionsValidees")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {

    @Id
    private String id;
    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean actif = true;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean doitChangerMotDePasse = false;

    private LocalDateTime derniereConnexion;

    @OneToMany(mappedBy = "valideePar")
    private List<Restitution> restitutionsValidees;

    public String getNomComplet() {
        return (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
    }

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}