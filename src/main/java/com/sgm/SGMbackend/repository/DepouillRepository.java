package com.sgm.SGMbackend.repository;

import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DepouillRepository extends JpaRepository<Depouille, Long> {

    Page<Depouille> findByStatut(StatutDepouille statut, Pageable pageable);

    @Query("SELECT d FROM Depouille d WHERE LOWER(d.nomDefunt) LIKE LOWER(CONCAT('%',:q,'%'))" +
            " OR LOWER(d.prenomDefunt) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<Depouille> findByNomOrPrenom(@Param("q") String q, Pageable pageable);

    List<Depouille> findByStatutAndNomDefuntIsNull(); // Non identifiées
}