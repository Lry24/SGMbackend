package com.sgm.SGMbackend.repository;

import com.sgm.SGMbackend.entity.Utilisateur;
import com.sgm.SGMbackend.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, String> {

    Optional<Utilisateur> findByEmail(String email);

    Page<Utilisateur> findByRole(Role role, Pageable pageable);

    Page<Utilisateur> findByActifTrue(Pageable pageable);
}