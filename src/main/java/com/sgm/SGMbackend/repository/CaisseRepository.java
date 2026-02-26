package com.sgm.SGMbackend.repository;

import com.sgm.SGMbackend.entity.Caisse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CaisseRepository extends JpaRepository<Caisse, Long> {

    @Query("SELECT c FROM Caisse c WHERE c.dateOuverture >= :startOfDay AND c.dateOuverture < :endOfDay")
    Optional<Caisse> findByDate(@Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    Optional<Caisse> findTopByEstFermeeFalseOrderByDateOuvertureDesc();
}
