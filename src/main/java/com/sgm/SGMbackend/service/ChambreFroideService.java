package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.entity.ChambreFroide;
import java.util.List;

public interface ChambreFroideService {
    ChambreFroide creer(String numero, int capacite, float tempCible);

    ChambreFroide modifier(Long id, int capacite, float tempCible);

    void enregistrerTemperature(Long chambreId, float temperature);

    ChambreFroide findById(Long id);

    List<ChambreFroide> findAll();

    List<ChambreFroide> cartographie();
}
