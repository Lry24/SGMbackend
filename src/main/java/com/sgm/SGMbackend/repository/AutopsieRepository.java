package com.sgm.SGMbackend.repository;

import com.sgm.SGMbackend.entity.Autopsie;
import com.sgm.SGMbackend.entity.enums.StatutAutopsie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AutopsieRepository extends JpaRepository<Autopsie, Long> {

    /**
     * Règle métier clé : vérifier qu'il n'existe pas déjà une autopsie active
     * (PLANIFIEE ou EN_COURS) pour cette dépouille.
     */
    boolean existsByDepouille_IdAndStatutIn(Long depouillId, Collection<StatutAutopsie> statuts);

    Page<Autopsie> findByStatut(StatutAutopsie statut, Pageable pageable);

    List<Autopsie> findByMedecinId(String medecinId);

    /**
     * Planning du jour ou de la semaine : autopsies planifiées entre deux dates.
     */
    @Query("SELECT a FROM Autopsie a WHERE a.datePlanifiee BETWEEN :debut AND :fin ORDER BY a.datePlanifiee")
    List<Autopsie> findPlanning(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
}
