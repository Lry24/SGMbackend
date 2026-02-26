package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.dto.dtoRequest.AlerteConfigRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.AlerteConfigResponseDTO;
import com.sgm.SGMbackend.dto.dtoResponse.AlerteResponseDTO;
import com.sgm.SGMbackend.entity.Alerte;
import com.sgm.SGMbackend.entity.AlerteConfig;
import com.sgm.SGMbackend.entity.ChambreFroide;
import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import com.sgm.SGMbackend.entity.enums.TypeAlerte;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.repository.AlerteConfigRepository;
import com.sgm.SGMbackend.repository.AlerteRepository;
import com.sgm.SGMbackend.repository.ChambreFroideRepository;
import com.sgm.SGMbackend.repository.DepouilleRepository;
import com.sgm.SGMbackend.service.AlerteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlerteServiceImpl implements AlerteService {

    private final AlerteRepository alerteRepo;
    private final AlerteConfigRepository configRepo;
    private final ChambreFroideRepository chambreRepo;
    private final DepouilleRepository depouilleRepo;

    @Override
    @Transactional
    public void verifierTemperature(Long chambreId, float temperature, float temperatureCible) {
        if (Math.abs(temperature - temperatureCible) > 2.0f) {
            String msg = "Chambre " + chambreId + " : température anormale (" + temperature + "°C)";
            creerAlerte(TypeAlerte.TEMPERATURE, msg, "RESPONSABLE");
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void verifierToutesTemperature() {
        List<ChambreFroide> chambres = chambreRepo.findAll();
        for (ChambreFroide cf : chambres) {
            if (cf.getTemperatureActuelle() != null && cf.getTemperatureCible() != null) {
                verifierTemperature(cf.getId(), cf.getTemperatureActuelle(), cf.getTemperatureCible());
            }
        }
    }

    @Override
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void verifierSaturationChambres() {
        List<ChambreFroide> chambres = chambreRepo.findAll();
        for (ChambreFroide cf : chambres) {
            if (cf.getCapacite() != null && cf.getCapacite() > 0) {
                long occupes = cf.getEmplacements().stream().filter(e -> Boolean.TRUE.equals(e.getOccupe())).count();
                float taux = (float) occupes / cf.getCapacite() * 100;
                if (taux > 85.0f) {
                    String msg = "Saturation critique Chambre " + cf.getNumero() + " : " + String.format("%.1f", taux)
                            + "%";
                    creerAlerte(TypeAlerte.SATURATION, msg, "RESPONSABLE");
                }
            }
        }
    }

    @Override
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void verifierDelaisReglementaires() {
        List<Depouille> depouilles = depouilleRepo.findAll();
        for (Depouille d : depouilles) {
            if (d.getStatut() != StatutDepouille.RESTITUEE && d.getDateArrivee() != null) {
                long jours = ChronoUnit.DAYS.between(d.getDateArrivee(), LocalDateTime.now());
                if (jours > 30) {
                    String msg = "Délai règlementaire dépassé pour " + d.getNomDefunt() + " (" + jours + " jours)";
                    creerAlerte(TypeAlerte.DELAI, msg, "ADMIN");
                }
            }
        }
    }

    @Override
    @Transactional
    public void creerAlerte(TypeAlerte type, String message, String roleDestinataire) {
        Alerte alerte = Alerte.builder()
                .type(type)
                .message(message)
                .roleDestinataire(roleDestinataire)
                .acquittee(false)
                .build();
        alerteRepo.save(alerte);
        log.error("ALERTE: [{}] {}", type, message);
    }

    @Override
    public Page<AlerteResponseDTO> findAll(TypeAlerte type, Pageable pageable) {
        Page<Alerte> alertes = (type != null)
                ? alerteRepo.findByTypeAndAcquitteeFalse(type, pageable)
                : alerteRepo.findByAcquitteeFalse(pageable);
        return alertes.map(this::mapToResponse);
    }

    @Override
    public List<AlerteConfigResponseDTO> findAllConfigs() {
        return configRepo.findAll().stream().map(this::mapConfigToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AlerteConfigResponseDTO saveConfig(AlerteConfigRequestDTO req) {
        AlerteConfig config = configRepo.findByType(req.getType())
                .orElse(new AlerteConfig());
        config.setType(req.getType());
        config.setSeuil(req.getSeuil());
        config.setCanal(req.getCanal());
        config.setDestinataires(req.getDestinataires());
        return mapConfigToResponse(configRepo.save(config));
    }

    @Override
    @Transactional
    public void acquitter(Long id, String commentaire) {
        Alerte alerte = alerteRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerte introuvable : " + id));
        alerte.setAcquittee(true);
        alerte.setDateAcquittement(LocalDateTime.now());
        alerte.setCommentaireAcquittement(commentaire);
        alerteRepo.save(alerte);
    }

    private AlerteResponseDTO mapToResponse(Alerte a) {
        return AlerteResponseDTO.builder()
                .id(a.getId())
                .type(a.getType())
                .message(a.getMessage())
                .roleDestinataire(a.getRoleDestinataire())
                .acquittee(a.getAcquittee())
                .dateCreation(a.getDateCreation())
                .dateAcquittement(a.getDateAcquittement())
                .commentaireAcquittement(a.getCommentaireAcquittement())
                .build();
    }

    private AlerteConfigResponseDTO mapConfigToResponse(AlerteConfig c) {
        return AlerteConfigResponseDTO.builder()
                .id(c.getId())
                .type(c.getType())
                .seuil(c.getSeuil())
                .canal(c.getCanal())
                .destinataires(c.getDestinataires())
                .build();
    }
}
