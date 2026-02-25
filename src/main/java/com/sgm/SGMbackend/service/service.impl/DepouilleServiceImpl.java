package com.sgm.SGMbackend.service.service.impl;

import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.repository.DepouillRepository;
import com.sgm.SGMbackend.service.DepouilleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.Year;

@Service
@RequiredArgsConstructor
@Transactional
public class DepouilleServiceImpl  {

    private final DepouillRepository repository;

    @Override
    public Depouille enregistrer(Depouille depouille) {

        depouille.setIdentifiantUnique(genererIdentifiant());
        depouille.setDateArrivee(LocalDateTime.now());
        depouille.setStatut(StatutDepouille.RECUE);

        return repository.save(depouille);
    }

    @Override
    public Depouille changerStatut(Long id, StatutDepouille nouveauStatut) {

        Depouille d = findById(id);
        validerTransition(d.getStatut(), nouveauStatut);
        d.setStatut(nouveauStatut);

        return repository.save(d);
    }

    @Override
    public void supprimer(Long id) {

        Depouille d = findById(id);

        if (d.getStatut() != StatutDepouille.RECUE)
            throw new BusinessRuleException(
                    "Suppression autorisée uniquement si statut RECUE");

        repository.delete(d);
    }

    @Override
    public Depouille findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Dépouille introuvable"));
    }

    @Override
    public List<String> historique(Long id) {
        findById(id);
        return List.of("Historique non implémenté");
    }
    @Override
    public Depouille modifier(Long id, Depouille updated) {

        Depouille existing = findById(id);

        existing.setNomDefunt(updated.getNomDefunt());
        existing.setPrenomDefunt(updated.getPrenomDefunt());
        existing.setCausePresumee(updated.getCausePresumee());
        existing.setProvenance(updated.getProvenance());
        existing.setObservations(updated.getObservations());

        return repository.save(existing);
    }
    @Override
    public Page<Depouille> findAll(Pageable pageable,
                                   String search,
                                   StatutDepouille statut) {

        if (search != null && !search.isBlank())
            return repository.findByNomOrPrenom(search, pageable);

        if (statut != null)
            return repository.findByStatut(statut, pageable);

        return repository.findAll(pageable);
    }

    private void validerTransition(StatutDepouille actuel,
                                   StatutDepouille nouveau) {

        boolean valide = switch (actuel) {

            case RECUE -> nouveau == StatutDepouille.EN_CHAMBRE_FROIDE;

            case EN_CHAMBRE_FROIDE ->
                    nouveau == StatutDepouille.EN_AUTOPSIE
                            || nouveau == StatutDepouille.PREPAREE;

            case EN_AUTOPSIE ->
                    nouveau == StatutDepouille.PREPAREE;

            case PREPAREE ->
                    nouveau == StatutDepouille.RESTITUEE;

            default -> false;
        };

        if (!valide)
            throw new BusinessRuleException(
                    "Transition invalide : " + actuel + " → " + nouveau);
    }

    private String genererIdentifiant() {

        int annee = Year.now().getValue();
        long count = repository.count() + 1;

        return String.format("SGM-%d-%05d", annee, count);
    }
}
