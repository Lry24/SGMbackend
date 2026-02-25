package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepouilleService {
    Depouille enregistrer(Depouille depouille);

    Depouille changerStatut(Long id, StatutDepouille nouveauStatut);

    void supprimer(Long id);

    Depouille findById(Long id);

    Page<Depouille> findAll(Pageable pageable, String search, StatutDepouille statut);

    byte[] getQRCode(Long id);
}
