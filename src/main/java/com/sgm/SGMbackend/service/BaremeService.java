package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.entity.Bareme;
import java.util.List;

public interface BaremeService {
    Bareme creer(Bareme bareme);

    Bareme modifier(Long id, Bareme bareme);

    void softDelete(Long id);

    List<Bareme> findAllActifs();

    Bareme findById(Long id);
}
