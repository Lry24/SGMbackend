package com.sgm.SGMbackend.service;

public interface AlerteService {
    void verifierTemperature(Long chambreId, float temperature, float temperatureCible);
}
