package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.entity.ChambreFroide;
import com.sgm.SGMbackend.entity.Emplacement;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.repository.ChambreFroideRepository;
import com.sgm.SGMbackend.repository.EmplacementRepository;
import com.sgm.SGMbackend.service.AlerteService;
import com.sgm.SGMbackend.service.ChambreFroideService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChambreFroideServiceImpl implements ChambreFroideService {

    private final ChambreFroideRepository chambreRepo;
    private final EmplacementRepository emplacementRepo;
    private final AlerteService alerteService;

    @Override
    @Transactional
    public ChambreFroide creer(String numero, int capacite, float tempCible) {
        // Créer la chambre froide
        ChambreFroide chambre = ChambreFroide.builder()
                .numero(numero)
                .capacite(capacite)
                .temperatureCible(tempCible)
                .build();
        chambre = chambreRepo.save(chambre);

        // RÈGLE 1 — Génération automatique des emplacements (E1, E2, E3...)
        List<Emplacement> emplacements = new ArrayList<>();
        for (int i = 1; i <= capacite; i++) {
            emplacements.add(Emplacement.builder()
                    .code(chambre.getNumero() + "-E" + i)
                    .occupe(false)
                    .chambreFroide(chambre)
                    .build());
        }
        emplacementRepo.saveAll(emplacements);

        return chambre;
    }

    @Override
    @Transactional
    public ChambreFroide modifier(Long id, int capacite, float tempCible) {
        ChambreFroide chambre = findById(id);
        chambre.setCapacite(capacite);
        chambre.setTemperatureCible(tempCible);
        return chambreRepo.save(chambre);
    }

    @Override
    @Transactional
    public void enregistrerTemperature(Long chambreId, float temperature) {
        ChambreFroide chambre = findById(chambreId);
        chambre.setTemperatureActuelle(temperature);
        chambreRepo.save(chambre);

        // RÈGLE 4 — Alerte température : Déclencher si écart > 2 degrés
        float cible = chambre.getTemperatureCible() != null ? chambre.getTemperatureCible() : -4.0f;
        if (Math.abs(temperature - cible) > 2.0) {
            alerteService.verifierTemperature(chambreId, temperature, cible);
        }
    }

    @Override
    public ChambreFroide findById(Long id) {
        return chambreRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chambre froide introuvable : " + id));
    }

    @Override
    public List<ChambreFroide> findAll() {
        return chambreRepo.findAll();
    }

    @Override
    public List<ChambreFroide> cartographie() {
        return chambreRepo.findAll();
    }
}
