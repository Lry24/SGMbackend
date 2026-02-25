package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.entity.ChambreFroide;
import com.sgm.SGMbackend.entity.Emplacement;

public interface ChambreFroideService {

    ChambreFroide creer(String numero,
                        int capacite,
                        float temperatureCible);

    Emplacement affecter(Long depouilleId,
                         Long emplacementId);

    void liberer(Long emplacementId);

    void enregistrerTemperature(Long chambreId,
                                float temperature);

    double calculerTauxOccupation(Long chambreId);
}