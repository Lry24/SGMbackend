package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepouilleService {

    Depouille enregistrer(Depouille depouille);

    Depouille modifier(Long id, Depouille updated);

    Depouille changerStatut(Long id, StatutDepouille nouveauStatut);

    void supprimer(Long id);

    Depouille findById(Long id);

    Page<Depouille> findAll(Pageable pageable,
                            String search,
                            List<String> historique(Long id);                     StatutDepouille statut);
}