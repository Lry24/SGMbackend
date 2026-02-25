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

    @org.springframework.data.jpa.repository.Query("SELECT d FROM Depouille d WHERE LOWER(d.nomDefunt) LIKE LOWER(CONCAT('%',:q,'%'))"
            +
            " OR LOWER(d.prenomDefunt) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<Depouille> findByNomOrPrenom(@org.springframework.data.repository.query.Param("q") String q,
            Pageable pageable);

    java.util.List<Depouille> findByStatutAndNomDefuntIsNull(StatutDepouille statut); // Dépouilles non identifiées

    boolean existsByIdentifiantUnique(String identifiantUnique);
}
