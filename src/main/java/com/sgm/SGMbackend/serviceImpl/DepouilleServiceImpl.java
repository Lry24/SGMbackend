package com.sgm.SGMbackend.serviceImpl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.repository.DepouilleRepository;
import com.sgm.SGMbackend.service.DepouilleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.Year;

@Service
@RequiredArgsConstructor
public class DepouilleServiceImpl implements DepouilleService {

    private final DepouilleRepository depouilleRepository;

    @Override
    @Transactional
    public Depouille enregistrer(Depouille depouille) {
        // RÈGLE 1 — Génération ID unique : Format SGM-AAAA-NNNNN
        depouille.setIdentifiantUnique(genererIdentifiantUnique());
        depouille.setDateArrivee(LocalDateTime.now());
        depouille.setStatut(StatutDepouille.RECUE);
        return depouilleRepository.save(depouille);
    }

    @Override
    @Transactional
    public Depouille changerStatut(Long id, StatutDepouille nouveauStatut) {
        Depouille d = findById(id);
        // RÈGLE 2 — Workflow statut (transitions autorisées)
        validerTransition(d.getStatut(), nouveauStatut);
        d.setStatut(nouveauStatut);
        return depouilleRepository.save(d);
    }

    @Override
    @Transactional
    public void supprimer(Long id) {
        Depouille d = findById(id);
        // RÈGLE 3 — Suppression autorisée SEULEMENT si statut == RECUE
        if (d.getStatut() != StatutDepouille.RECUE) {
            throw new BusinessRuleException(
                    "Seule une dépouille en statut RECUE peut être supprimée (erreur de saisie).");
        }
        depouilleRepository.delete(d);
    }

    @Override
    public Depouille findById(Long id) {
        return depouilleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dépouille introuvable avec l'ID : " + id));
    }

    @Override
    public Page<Depouille> findAll(Pageable pageable, String search, StatutDepouille statut) {
        if (search != null && !search.isBlank()) {
            return depouilleRepository.findByNomOrPrenom(search, pageable);
        }
        if (statut != null) {
            return depouilleRepository.findByStatut(statut, pageable);
        }
        return depouilleRepository.findAll(pageable);
    }

    @Override
    public byte[] getQRCode(Long id) {
        Depouille d = findById(id);
        // RÈGLE 4 — QR Code : Générer à la volée
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(d.getIdentifiantUnique(), BarcodeFormat.QR_CODE, 250, 250);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du QR Code", e);
        }
    }

    private String genererIdentifiantUnique() {
        int annee = Year.now().getValue();
        long count = depouilleRepository.count() + 1;
        return String.format("SGM-%d-%05d", annee, count);
    }

    private void validerTransition(StatutDepouille actuel, StatutDepouille nouveau) {
        boolean valide = switch (actuel) {
            case RECUE -> nouveau == StatutDepouille.EN_CHAMBRE_FROIDE;
            case EN_CHAMBRE_FROIDE -> nouveau == StatutDepouille.EN_AUTOPSIE || nouveau == StatutDepouille.PREPAREE;
            case EN_AUTOPSIE -> nouveau == StatutDepouille.PREPAREE;
            case PREPAREE -> nouveau == StatutDepouille.RESTITUEE;
            default -> false;
        };

        if (!valide) {
            throw new BusinessRuleException("Transition de statut invalide : " + actuel + " -> " + nouveau);
        }
    }
}
