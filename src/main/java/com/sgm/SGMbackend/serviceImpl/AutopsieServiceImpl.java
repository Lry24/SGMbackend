package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.entity.Autopsie;
import com.sgm.SGMbackend.entity.enums.StatutAutopsie;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.repository.AutopsieRepository;
import com.sgm.SGMbackend.repository.DepouilleRepository;
import com.sgm.SGMbackend.service.AutopsieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AutopsieServiceImpl implements AutopsieService {

    private final AutopsieRepository autoRepository;
    private final DepouilleRepository depouilleRepository;

    @Override
    @Transactional
    public Autopsie planifier(Long depouillId, String medecinId, LocalDateTime datePlanifiee) {
        var depouille = depouilleRepository.findById(depouillId)
                .orElseThrow(() -> new ResourceNotFoundException("Dépouille introuvable : " + depouillId));

        boolean autoActive = autoRepository.existsByDepouille_IdAndStatutIn(
                depouillId, List.of(StatutAutopsie.PLANIFIEE, StatutAutopsie.EN_COURS));
        if (autoActive) {
            throw new BusinessRuleException("Cette dépouille a déjà une autopsie active.");
        }

        depouille.setStatut(StatutDepouille.EN_AUTOPSIE);
        depouilleRepository.save(depouille);

        Autopsie a = Autopsie.builder()
                .depouille(depouille)
                .medecinId(medecinId)
                .datePlanifiee(datePlanifiee)
                .statut(StatutAutopsie.PLANIFIEE)
                .build();
        return autoRepository.save(a);
    }

    @Override
    @Transactional
    public Autopsie demarrer(Long id) {
        Autopsie a = findById(id);
        if (a.getStatut() != StatutAutopsie.PLANIFIEE)
            throw new BusinessRuleException("Seule une autopsie PLANIFIEE peut être démarrée.");
        a.setStatut(StatutAutopsie.EN_COURS);
        a.setDateDebut(LocalDateTime.now());
        return autoRepository.save(a);
    }

    @Override
    @Transactional
    public Autopsie terminer(Long id, String rapport, String conclusion) {
        Autopsie a = findById(id);
        if (a.getStatut() != StatutAutopsie.EN_COURS)
            throw new BusinessRuleException("Seule une autopsie EN_COURS peut être terminée.");
        a.setRapport(rapport);
        a.setConclusion(conclusion);
        a.setStatut(StatutAutopsie.TERMINEE);
        a.setDateFin(LocalDateTime.now());

        var dep = a.getDepouille();
        dep.setStatut(StatutDepouille.PREPAREE);
        depouilleRepository.save(dep);

        return autoRepository.save(a);
    }

    @Override
    public Autopsie findById(Long id) {
        return autoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autopsie introuvable : " + id));
    }

    @Override
    public Page<Autopsie> findAll(Pageable pageable, StatutAutopsie statut) {
        if (statut != null)
            return autoRepository.findByStatut(statut, pageable);
        return autoRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Autopsie ajouterAnalyse(Long id, String description) {
        Autopsie a = findById(id);
        String analyses = a.getAnalysesComplementaires();
        if (analyses == null) {
            analyses = description;
        } else {
            analyses += "\n---\n" + description;
        }
        a.setAnalysesComplementaires(analyses);
        return autoRepository.save(a);
    }

    @Override
    @Transactional
    public void annuler(Long id) {
        Autopsie a = findById(id);
        if (a.getStatut() != StatutAutopsie.PLANIFIEE) {
            throw new BusinessRuleException("Seule une autopsie PLANIFIEE peut être annulée.");
        }

        var dep = a.getDepouille();
        // Si on annule, on remet théoriquement en chambre froide si elle y était
        // Pour simplifier, on remet en EN_CHAMBRE_FROIDE ou RECUE ?
        // On va dire EN_CHAMBRE_FROIDE par défaut si on vient d'une planification
        dep.setStatut(StatutDepouille.EN_CHAMBRE_FROIDE);
        depouilleRepository.save(dep);

        autoRepository.delete(a);
    }

    @Override
    public List<Autopsie> findByMedecin(String medecinId) {
        return autoRepository.findByMedecinId(medecinId);
    }

    @Override
    public List<Autopsie> getPlanning(LocalDateTime date) {
        LocalDateTime debut = date.with(LocalTime.MIN);
        LocalDateTime fin = date.with(LocalTime.MAX);
        return autoRepository.findPlanning(debut, fin);
    }
}
