package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.dto.dtoRequest.RestitutionRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.RestitutionResponseDTO;
import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.Famille;
import com.sgm.SGMbackend.entity.Restitution;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import com.sgm.SGMbackend.entity.enums.StatutFacture;
import com.sgm.SGMbackend.entity.enums.StatutRestitution;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.dto.dtoResponse.FactureResponseDTO;
import com.sgm.SGMbackend.event.RestitutionPlanifieeEvent;
import com.sgm.SGMbackend.mapper.FactureMapper;
import com.sgm.SGMbackend.mapper.RestitutionMapper;
import com.sgm.SGMbackend.repository.*;
import com.sgm.SGMbackend.service.RestitutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestitutionServiceImpl implements RestitutionService {

    private final RestitutionRepository restitutionRepository;
    private final DepouilleRepository depouilleRepository;
    private final FamilleRepository familleRepository;
    private final FactureRepository factureRepository;
    private final RestitutionMapper restitutionMapper;
    private final FactureMapper factureMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public RestitutionResponseDTO planifier(RestitutionRequestDTO requestDTO) {
        Depouille depouille = depouilleRepository.findById(requestDTO.getDepouilleId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Dépouille introuvable : " + requestDTO.getDepouilleId()));

        // Nouveau : Vérifier que la facture est payée dès la planification
        factureRepository.findByDepouille_Id(depouille.getId())
                .ifPresentOrElse(f -> {
                    if (f.getStatut() != StatutFacture.PAYEE) {
                        throw new BusinessRuleException("La planification n'est possible que si la facture est PAYEE.");
                    }
                }, () -> {
                    throw new BusinessRuleException(
                            "Aucune facture trouvée pour cette dépouille. Le paiement est requis.");
                });

        Famille famille = familleRepository.findById(requestDTO.getFamilleId())
                .orElseThrow(() -> new ResourceNotFoundException("Famille introuvable : " + requestDTO.getFamilleId()));

        Restitution restitution = restitutionMapper.toEntity(requestDTO);
        restitution.setDepouille(depouille);
        restitution.setFamille(famille);
        restitution.setStatut(StatutRestitution.PLANIFIEE);

        Restitution saved = restitutionRepository.save(restitution);
        eventPublisher.publishEvent(new RestitutionPlanifieeEvent(this, saved));

        return restitutionMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public RestitutionResponseDTO confirmer(Long id) {
        Restitution r = restitutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restitution introuvable : " + id));

        if (r.getStatut() != StatutRestitution.PLANIFIEE) {
            throw new BusinessRuleException("Seule une restitution PLANIFIEE peut être confirmée.");
        }

        // Règle 1 : Vérifier la facture (doit être PAYEE)
        factureRepository.findByDepouille_Id(r.getDepouille().getId())
                .ifPresentOrElse(f -> {
                    if (f.getStatut() != StatutFacture.PAYEE) {
                        throw new BusinessRuleException("La facture associée doit être PAYEE avant confirmation.");
                    }
                }, () -> {
                    throw new BusinessRuleException("Aucune facture trouvée pour cette dépouille.");
                });

        // Règle 2 : Statut dépouille (PREPAREE ou EN_CHAMBRE_FROIDE)
        StatutDepouille currentStatut = r.getDepouille().getStatut();
        if (currentStatut != StatutDepouille.PREPAREE && currentStatut != StatutDepouille.EN_CHAMBRE_FROIDE) {
            throw new BusinessRuleException(
                    "La dépouille n'est pas dans un état permettant la restitution (actuel: " + currentStatut + ").");
        }

        r.setFacturesSoldees(true);
        r.setDocumentsComplets(true); // À affiner si besoin de vérifier l'entité Document
        r.setStatut(StatutRestitution.CONFIRMEE);

        return restitutionMapper.toResponseDTO(restitutionRepository.save(r));
    }

    @Override
    @Transactional
    public void annuler(Long id, String motif) {
        Restitution r = restitutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restitution introuvable : " + id));

        r.setStatut(StatutRestitution.ANNULEE);
        r.setMotifAnnulation(motif);
        restitutionRepository.save(r);
    }

    @Override
    @Transactional
    public RestitutionResponseDTO effectuer(Long id, String pieceIdentiteRef) {
        Restitution r = restitutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restitution introuvable : " + id));

        if (r.getStatut() != StatutRestitution.CONFIRMEE) {
            throw new BusinessRuleException("La restitution doit être CONFIRMEE avant d'être effectuée.");
        }

        r.setPieceIdentiteRef(pieceIdentiteRef);
        r.setDateEffective(LocalDateTime.now());
        r.setStatut(StatutRestitution.EFFECTUEE);

        // Mettre à jour la dépouille
        Depouille dep = r.getDepouille();
        dep.setStatut(StatutDepouille.RESTITUEE);

        // Libérer l'emplacement
        if (dep.getEmplacement() != null) {
            dep.getEmplacement().setOccupe(false);
            dep.setEmplacement(null);
        }

        depouilleRepository.save(dep);
        return restitutionMapper.toResponseDTO(restitutionRepository.save(r));
    }

    @Override
    public RestitutionResponseDTO findById(Long id) {
        return restitutionRepository.findById(id)
                .map(restitutionMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Restitution introuvable : " + id));
    }

    @Override
    public Page<RestitutionResponseDTO> findAll(StatutRestitution statut, Pageable pageable) {
        Page<Restitution> page = (statut != null)
                ? restitutionRepository.findByStatut(statut, pageable)
                : restitutionRepository.findAll(pageable);
        return page.map(restitutionMapper::toResponseDTO);
    }

    @Override
    public List<RestitutionResponseDTO> getPlanning(LocalDateTime date) {
        LocalDateTime debut = date.toLocalDate().atStartOfDay();
        LocalDateTime fin = date.toLocalDate().atTime(23, 59, 59);
        return restitutionRepository.findPlanning(debut, fin).stream()
                .map(restitutionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FactureResponseDTO getFactureByDepouille(Long depouilleId) {
        return factureRepository.findByDepouille_Id(depouilleId)
                .map(factureMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucune facture trouvée pour la dépouille : " + depouilleId));
    }
}
