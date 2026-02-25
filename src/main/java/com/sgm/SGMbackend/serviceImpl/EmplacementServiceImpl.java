package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.Emplacement;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.repository.DepouilleRepository;
import com.sgm.SGMbackend.repository.EmplacementRepository;
import com.sgm.SGMbackend.service.EmplacementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmplacementServiceImpl implements EmplacementService {

    private final EmplacementRepository emplacementRepo;
    private final DepouilleRepository depouilleRepo;

    @Override
    @Transactional
    public Emplacement affecter(Long depouilleId, Long emplacementId) {
        Emplacement emp = findById(emplacementId);

        // RÈGLE METIER 2 : vérifier que l'emplacement est libre
        if (Boolean.TRUE.equals(emp.getOccupe())) {
            throw new BusinessRuleException("Cet emplacement est déjà occupé.");
        }

        Depouille d = depouilleRepo.findById(depouilleId)
                .orElseThrow(() -> new ResourceNotFoundException("Dépouille introuvable : " + depouilleId));

        // Mettre à jour l'emplacement
        emp.setDepouille(d);
        emp.setOccupe(true);
        emp.setDateAffectation(LocalDateTime.now());

        // RÈGLE METIER 2 : Mettre à jour statut dépouille -> EN_CHAMBRE_FROIDE
        d.setStatut(StatutDepouille.EN_CHAMBRE_FROIDE);
        d.setEmplacement(emp);
        depouilleRepo.save(d);

        return emplacementRepo.save(emp);
    }

    @Override
    @Transactional
    public Emplacement liberer(Long id, String motif) {
        Emplacement emp = findById(id);

        // RÈGLE 3 — Libération emplacement
        if (emp.getDepouille() != null) {
            Depouille d = emp.getDepouille();
            d.setEmplacement(null);
            // On ne change pas le statut ici car il peut passer à EN_AUTOPSIE ou PREPAREE
            // via d'autres services
            depouilleRepo.save(d);
        }

        emp.setOccupe(false);
        emp.setDepouille(null);
        emp.setDateAffectation(null);

        return emplacementRepo.save(emp);
    }

    @Override
    public List<Emplacement> findDisponibles() {
        return emplacementRepo.findByOccupeFalse();
    }

    @Override
    public List<Emplacement> findByChambre(Long chambreId, Boolean occupe) {
        if (occupe != null) {
            return emplacementRepo.findByChambreFroide_IdAndOccupe(chambreId, occupe);
        }
        return emplacementRepo.findByChambreFroide_Id(chambreId);
    }

    @Override
    public Emplacement findById(Long id) {
        return emplacementRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emplacement introuvable : " + id));
    }
}
