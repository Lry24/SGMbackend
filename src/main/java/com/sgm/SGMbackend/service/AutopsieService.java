package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.entity.Autopsie;
import com.sgm.SGMbackend.entity.enums.StatutAutopsie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface AutopsieService {
    Autopsie planifier(Long depouillId, String medecinId, LocalDateTime datePlanifiee);

    Autopsie demarrer(Long id);

    Autopsie terminer(Long id, String rapport, String conclusion);

    Autopsie findById(Long id);

    Page<Autopsie> findAll(Pageable pageable, StatutAutopsie statut);

    Autopsie ajouterAnalyse(Long id, String description);

    void annuler(Long id);

    List<Autopsie> findByMedecin(String medecinId);

    List<Autopsie> getPlanning(LocalDateTime date);
}
