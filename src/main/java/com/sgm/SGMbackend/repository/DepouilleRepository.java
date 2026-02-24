package com.sgm.SGMbackend.repository;

import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepouilleRepository extends JpaRepository<Depouille, Long> {

    Optional<Depouille> findByIdentifiantUnique(String identifiantUnique);

    Page<Depouille> findByStatut(StatutDepouille statut, Pageable pageable);

    boolean existsByIdentifiantUnique(String identifiantUnique);
}
