package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.entity.Autopsie;
import com.sgm.SGMbackend.entity.enums.StatutAutopsie;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.repository.AutopsieRepository;
import com.sgm.SGMbackend.repository.DepouilleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service métier du module Autopsies.
 *
 * Règles métier appliquées :
 * - Une dépouille ne peut avoir qu'une seule autopsie PLANIFIEE ou EN_COURS à
 * la fois
 * - Seule une autopsie PLANIFIEE peut être démarrée
 * - Seule une autopsie EN_COURS peut être terminée
 * - Seule une autopsie PLANIFIEE peut être annulée (DELETE)
 * - Lorsqu'une autopsie démarre, la dépouille passe à EN_AUTOPSIE
 * - Lorsqu'une autopsie se termine, la dépouille repasse à PREPAREE
 */
@Service
@RequiredArgsConstructor
public class AutopsieService {

    private final AutopsieRepository autoRepository;
    private final DepouilleRepository depouillRepository;

    /**
     * Planifier une nouvelle autopsie pour une dépouille.
     * Vérifie : existence dépouille + absence d'autopsie active.
     */
    @Transactional
    public Autopsie planifier(Long depouillId, String medecinId, LocalDateTime datePlanifiee) {
        // Règle 1 : la dépouille doit exister
        var depouill = depouillRepository.findById(depouillId)
                .orElseThrow(() -> new ResourceNotFoundException("Dépouille introuvable : " + depouillId));

        // Règle 2 : pas d'autopsie active en cours sur cette dépouille
        boolean autoActive = autoRepository.existsByDepouille_IdAndStatutIn(
                depouillId, List.of(StatutAutopsie.PLANIFIEE, StatutAutopsie.EN_COURS));
        if (autoActive) {
            throw new BusinessRuleException(
                    "Cette dépouille a déjà une autopsie active (PLANIFIEE ou EN_COURS).");
        }

        // NOTE : la dépouille passe à EN_AUTOPSIE uniquement au DÉMARRAGE (demarrer())
        // Ici on crée simplement la planification
        Autopsie a = Autopsie.builder()
                .depouille(depouill)
                .medecinId(medecinId)
                .datePlanifiee(datePlanifiee)
                .statut(StatutAutopsie.PLANIFIEE)
                .build();
        return autoRepository.save(a);
    }

    /**
     * Démarrer une autopsie planifiée → statut passe à EN_COURS.
     */
    @Transactional
    public Autopsie demarrer(Long id) {
        Autopsie a = findById(id);
        if (a.getStatut() != StatutAutopsie.PLANIFIEE) {
            throw new BusinessRuleException("Seule une autopsie PLANIFIEE peut être démarrée.");
        }
        // La dépouille passe à EN_AUTOPSIE au démarrage effectif de l'autopsie
        var dep = a.getDepouille();
        dep.setStatut(StatutDepouille.EN_AUTOPSIE);
        depouillRepository.save(dep);

        a.setStatut(StatutAutopsie.EN_COURS);
        a.setDateDebut(LocalDateTime.now());
        return autoRepository.save(a);
    }

    /**
     * Terminer une autopsie en cours : enregistre rapport + conclusion,
     * remet la dépouille à l'état PREPAREE.
     */
    @Transactional
    public Autopsie terminer(Long id, String rapport, String conclusion) {
        Autopsie a = findById(id);
        if (a.getStatut() != StatutAutopsie.EN_COURS) {
            throw new BusinessRuleException("Seule une autopsie EN_COURS peut être terminée.");
        }
        a.setRapport(rapport);
        a.setConclusion(conclusion);
        a.setStatut(StatutAutopsie.TERMINEE);
        a.setDateFin(LocalDateTime.now());

        // Remettre la dépouille en statut PREPAREE (prête pour restitution)
        var dep = a.getDepouille();
        dep.setStatut(StatutDepouille.PREPAREE);
        depouillRepository.save(dep);

        return autoRepository.save(a);
    }

    /**
     * Ajouter une analyse complémentaire (concaténation au champ existant).
     */
    @Transactional
    public Autopsie ajouterAnalyse(Long id, String description) {
        Autopsie a = findById(id);
        if (a.getStatut() == StatutAutopsie.RAPPORT_VALIDE) {
            throw new BusinessRuleException("Impossible d'ajouter une analyse : le rapport est déjà validé.");
        }
        String existing = a.getAnalysesComplementaires();
        String updated = (existing == null || existing.isBlank())
                ? description
                : existing + "\n---\n" + description;
        a.setAnalysesComplementaires(updated);
        return autoRepository.save(a);
    }

    /**
     * Annuler (supprimer) une autopsie — seulement si PLANIFIEE.
     * Remet la dépouille dans son état précédent (EN_CHAMBRE_FROIDE).
     */
    @Transactional
    public void annuler(Long id) {
        Autopsie a = findById(id);
        if (a.getStatut() != StatutAutopsie.PLANIFIEE) {
            throw new BusinessRuleException(
                    "Seule une autopsie PLANIFIEE peut être annulée. Statut actuel : " + a.getStatut());
        }
        // Remettre la dépouille dans un état cohérent
        var dep = a.getDepouille();
        dep.setStatut(StatutDepouille.EN_CHAMBRE_FROIDE);
        depouillRepository.save(dep);

        autoRepository.delete(a);
    }

    /**
     * Récupérer une autopsie par son ID ou lever 404.
     */
    public Autopsie findById(Long id) {
        return autoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autopsie introuvable : " + id));
    }

    /**
     * Liste paginée avec filtre optionnel par statut.
     */
    public Page<Autopsie> findAll(Pageable pageable, StatutAutopsie statut) {
        if (statut != null)
            return autoRepository.findByStatut(statut, pageable);
        return autoRepository.findAll(pageable);
    }

    /**
     * Toutes les autopsies d'un médecin donné.
     */
    public List<Autopsie> findByMedecin(String medecinId) {
        return autoRepository.findByMedecinId(medecinId);
    }

    /**
     * Planning du jour (00:00 à 23:59) ou d'une journée passée en paramètre.
     */
    public List<Autopsie> getPlanning(LocalDateTime dateRef) {
        LocalDateTime debut = dateRef.toLocalDate().atStartOfDay();
        LocalDateTime fin = dateRef.toLocalDate().atTime(23, 59, 59);
        return autoRepository.findPlanning(debut, fin);
    }
}
