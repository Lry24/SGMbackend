package com.sgm.SGMbackend.repository;

import com.sgm.SGMbackend.entity.Restitution;
import com.sgm.SGMbackend.entity.enums.StatutRestitution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RestitutionRepository extends JpaRepository<Restitution, Long> {

    Page<Restitution> findByStatut(StatutRestitution statut, Pageable pageable);

    /**
     * Planning du jour : restitutions planifiées entre deux dates.
     */
    @Query("SELECT r FROM Restitution r WHERE r.datePlanifiee BETWEEN :debut AND :fin ORDER BY r.datePlanifiee")
    List<Restitution> findPlanning(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
}
