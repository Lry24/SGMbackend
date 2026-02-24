package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.entity.Restitution;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import com.sgm.SGMbackend.entity.enums.StatutFacture;
import com.sgm.SGMbackend.entity.enums.StatutRestitution;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.repository.DepouilleRepository;
import com.sgm.SGMbackend.repository.FamilleRepository;
import com.sgm.SGMbackend.repository.FactureRepository;
import com.sgm.SGMbackend.repository.RestitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service métier du module Restitutions.
 *
 * Règle de confirmation (3 pré-requis) :
 * 1. La facture de la dépouille doit être entièrement PAYEE
 * 2. La famille doit être identifiée (toujours vrai car déjà liée)
 * 3. La dépouille doit être dans un état compatible (PREPAREE ou
 * EN_CHAMBRE_FROIDE)
 *
 * Règle effectuée :
 * - La dépouille passe à RESTITUEE
 * - L'emplacement (chambre froide) est libéré si encore occupé
 */
@Service
@RequiredArgsConstructor
public class RestitutionService {

    private final RestitutionRepository restitutionRepository;
    private final DepouilleRepository depouillRepository;
    private final FamilleRepository familleRepository;
    private final FactureRepository factureRepository;

    /**
     * Planifier une restitution : lier dépouille + famille + date + représentant.
     */
    @Transactional
    public Restitution planifier(Long depouillId, Long familleId,
            LocalDateTime datePlanifiee, String representant) {
        var depouill = depouillRepository.findById(depouillId)
                .orElseThrow(() -> new ResourceNotFoundException("Dépouille introuvable : " + depouillId));
        var famille = familleRepository.findById(familleId)
                .orElseThrow(() -> new ResourceNotFoundException("Famille introuvable : " + familleId));

        Restitution r = Restitution.builder()
                .depouille(depouill)
                .famille(famille)
                .datePlanifiee(datePlanifiee)
                .representantFamille(representant)
                .statut(StatutRestitution.PLANIFIEE)
                .build();
        return restitutionRepository.save(r);
    }

    /**
     * ⭐ Confirmer une restitution — méthode clé qui vérifie les 3 pré-requis.
     */
    @Transactional
    public Restitution confirmer(Long id) {
        Restitution r = findById(id);
        if (r.getStatut() != StatutRestitution.PLANIFIEE) {
            throw new BusinessRuleException(
                    "Seule une restitution PLANIFIEE peut être confirmée. Statut actuel : " + r.getStatut());
        }

        Long depouillId = r.getDepouille().getId();

        // ✅ Pré-requis 1 : facture entièrement soldée
        var facture = factureRepository.findByDepouille_Id(depouillId).orElse(null);
        if (facture == null || facture.getStatut() != StatutFacture.PAYEE) {
            throw new BusinessRuleException(
                    "Impossible de confirmer : la facture doit être entièrement payée (statut PAYEE).");
        }

        // ✅ Pré-requis 2 : famille identifiée (garantie par la relation JPA)

        // ✅ Pré-requis 3 : dépouille dans un état compatible
        var dep = r.getDepouille();
        if (dep.getStatut() != StatutDepouille.PREPAREE
                && dep.getStatut() != StatutDepouille.EN_CHAMBRE_FROIDE) {
            throw new BusinessRuleException(
                    "La dépouille n'est pas dans un état permettant la restitution. Statut actuel : "
                            + dep.getStatut());
        }

        r.setFacturesSoldees(true);
        r.setDocumentsComplets(true);
        r.setStatut(StatutRestitution.CONFIRMEE);
        return restitutionRepository.save(r);
    }

    /**
     * Effectuer la restitution (dépouille remise physiquement à la famille).
     * La dépouille passe à RESTITUEE et l'emplacement est libéré.
     */
    @Transactional
    public Restitution effectuer(Long id, String pieceIdentiteRef) {
        Restitution r = findById(id);
        if (r.getStatut() != StatutRestitution.CONFIRMEE) {
            throw new BusinessRuleException(
                    "La restitution doit être CONFIRMEE avant d'être effectuée. Statut actuel : " + r.getStatut());
        }

        r.setPieceIdentiteRef(pieceIdentiteRef);
        r.setDateEffective(LocalDateTime.now());
        r.setStatut(StatutRestitution.EFFECTUEE);

        // Mettre à jour le statut de la dépouille → RESTITUEE
        var dep = r.getDepouille();
        dep.setStatut(StatutDepouille.RESTITUEE);

        // Libérer l'emplacement si encore occupé
        if (dep.getEmplacement() != null) {
            dep.getEmplacement().setOccupe(false);
            dep.setEmplacement(null);
        }
        depouillRepository.save(dep);

        return restitutionRepository.save(r);
    }

    /**
     * Annuler une restitution avec un motif obligatoire.
     */
    @Transactional
    public Restitution annuler(Long id, String motif) {
        Restitution r = findById(id);
        if (r.getStatut() == StatutRestitution.EFFECTUEE) {
            throw new BusinessRuleException(
                    "Une restitution EFFECTUEE ne peut plus être annulée.");
        }
        if (motif == null || motif.isBlank()) {
            throw new BusinessRuleException("Un motif d'annulation est obligatoire.");
        }
        r.setMotifAnnulation(motif);
        r.setStatut(StatutRestitution.ANNULEE);
        return restitutionRepository.save(r);
    }

    /**
     * Récupérer une restitution par son ID ou lever 404.
     */
    public Restitution findById(Long id) {
        return restitutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restitution introuvable : " + id));
    }

    /**
     * Liste paginée avec filtre optionnel par statut.
     */
    public Page<Restitution> findAll(Pageable pageable, StatutRestitution statut) {
        if (statut != null)
            return restitutionRepository.findByStatut(statut, pageable);
        return restitutionRepository.findAll(pageable);
    }

    /**
     * Planning du jour (00:00 à 23:59) ou d'une journée passée en paramètre.
     */
    public List<Restitution> getPlanning(LocalDateTime dateRef) {
        LocalDateTime debut = dateRef.toLocalDate().atStartOfDay();
        LocalDateTime fin = dateRef.toLocalDate().atTime(23, 59, 59);
        return restitutionRepository.findPlanning(debut, fin);
    }
}
