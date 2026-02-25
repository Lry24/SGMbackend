package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.service.AlerteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AlerteServiceImpl implements AlerteService {

    @Override
    public void verifierTemperature(Long chambreId, float temperature, float temperatureCible) {
        log.warn("ALERTE TEMPÉRATURE - Chambre {}: Actuelle {}°C / Cible {}°C",
                chambreId, temperature, temperatureCible);
        // Squelette : Sera complété par DEV_A
    }
}
