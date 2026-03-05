package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.entity.Emplacement;
import java.util.List;

public interface EmplacementService {
    Emplacement affecter(Long depouilleId, Long emplacementId, java.time.LocalDateTime date);

    Emplacement liberer(Long id, String motif);

    List<Emplacement> findDisponibles();

    List<Emplacement> findByChambre(Long chambreId, Boolean occupe);

    Emplacement findById(Long id);
}
