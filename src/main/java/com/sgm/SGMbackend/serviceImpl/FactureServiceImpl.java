package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.dto.dtoRequest.FactureRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.FactureResponseDTO;
import com.sgm.SGMbackend.dto.dtoResponse.PaiementResponseDTO;
import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.Facture;
import com.sgm.SGMbackend.entity.Famille;
import com.sgm.SGMbackend.entity.LigneFacture;
import com.sgm.SGMbackend.entity.Paiement;
import com.sgm.SGMbackend.entity.enums.StatutFacture;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.mapper.FactureMapper;
import com.sgm.SGMbackend.mapper.LigneFactureMapper;
import com.sgm.SGMbackend.repository.DepouilleRepository;
import com.sgm.SGMbackend.repository.FactureRepository;
import com.sgm.SGMbackend.repository.FamilleRepository;
import com.sgm.SGMbackend.repository.LigneFactureRepository;
import com.sgm.SGMbackend.repository.PaiementRepository;
import com.sgm.SGMbackend.service.FactureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FactureServiceImpl implements FactureService {

    private final FactureRepository factureRepository;
    private final LigneFactureRepository ligneFactureRepository;
    private final DepouilleRepository depouilleRepository;
    private final FamilleRepository familleRepository;
    private final PaiementRepository paiementRepository;
    private final FactureMapper factureMapper;
    private final LigneFactureMapper ligneFactureMapper;

    @Override
    public Page<FactureResponseDTO> findAll(StatutFacture statut, Long familleId, String dateDebut, String dateFin,
            Pageable pageable) {
        // Pour simplifier on ignore les dates dans le repo existant
        if (familleId != null && statut != null) {
            return factureRepository.findByStatutAndFamille_Id(statut, familleId, pageable)
                    .map(factureMapper::toResponseDTO);
        }
        return factureRepository.findAll(pageable).map(factureMapper::toResponseDTO);
    }

    @Override
    public FactureResponseDTO findById(Long id) {
        return factureMapper.toResponseDTO(findEntityById(id));
    }

    @Override
    public FactureResponseDTO getByDepouilleId(Long id) {
        Facture f = factureRepository.findByDepouille_Id(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune facture pour cette dépouille"));
        return factureMapper.toResponseDTO(f);
    }

    @Override
    public FactureResponseDTO calculer(Long depouillId) {
        // En vrai: récupérer les prestations prévues (si existantes)
        // Ici, on retourne une facture fictive pour estimation.
        Facture f = new Facture();
        f.setMontantTotal(0.0);
        return factureMapper.toResponseDTO(f);
    }

    @Override
    @Transactional
    public FactureResponseDTO creer(FactureRequestDTO dto) {
        Depouille depouille = depouilleRepository.findById(dto.getDepouilleId())
                .orElseThrow(() -> new ResourceNotFoundException("Dépouille introuvable"));
        Famille famille = familleRepository.findById(dto.getFamilleId())
                .orElseThrow(() -> new ResourceNotFoundException("Famille introuvable"));

        List<LigneFacture> lignes = dto.getLignes().stream()
                .map(ligneFactureMapper::toEntity).collect(Collectors.toList());

        double total = lignes.stream()
                .mapToDouble(l -> l.getQuantite() * l.getPrixUnitaire()).sum();
        double totalAvecRemise = total - (dto.getRemise() != null ? dto.getRemise() : 0.0);

        Facture f = Facture.builder()
                .numero(genererNumero())
                .depouille(depouille).famille(famille)
                .montantTotal(totalAvecRemise)
                .montantPaye(0.0).remise(dto.getRemise() != null ? dto.getRemise() : 0.0)
                .statut(StatutFacture.BROUILLON)
                .dateEmission(LocalDateTime.now())
                .build();
        f = factureRepository.save(f);

        final Facture factureFinal = f;
        lignes.forEach(l -> l.setFacture(factureFinal));
        ligneFactureRepository.saveAll(lignes);

        f.setLignes(lignes);
        return factureMapper.toResponseDTO(f);
    }

    @Override
    @Transactional
    public FactureResponseDTO modifier(Long id, FactureRequestDTO dto) {
        Facture f = findEntityById(id);
        if (f.getStatut() != StatutFacture.BROUILLON) {
            throw new BusinessRuleException("Seul un BROUILLON peut être modifié.");
        }
        // Pour simplifier on supprime les anciennes lignes et on met les nouvelles
        ligneFactureRepository.deleteAll(f.getLignes());

        List<LigneFacture> lignes = dto.getLignes().stream()
                .map(ligneFactureMapper::toEntity).collect(Collectors.toList());

        double total = lignes.stream()
                .mapToDouble(l -> l.getQuantite() * l.getPrixUnitaire()).sum();
        double totalAvecRemise = total - (dto.getRemise() != null ? dto.getRemise() : 0.0);

        f.setMontantTotal(totalAvecRemise);
        f.setRemise(dto.getRemise() != null ? dto.getRemise() : 0.0);

        final Facture factureFinal = f;
        lignes.forEach(l -> l.setFacture(factureFinal));
        ligneFactureRepository.saveAll(lignes);
        f.setLignes(lignes);

        return factureMapper.toResponseDTO(factureRepository.save(f));
    }

    @Override
    @Transactional
    public FactureResponseDTO emettre(Long id) {
        Facture f = findEntityById(id);
        if (f.getStatut() != StatutFacture.BROUILLON)
            throw new BusinessRuleException("Seul un BROUILLON peut être émis.");
        f.setStatut(StatutFacture.EMISE);
        f.setDateEmission(LocalDateTime.now());
        return factureMapper.toResponseDTO(factureRepository.save(f));
    }

    @Override
    @Transactional
    public PaiementResponseDTO enregistrerPaiement(Long id, Double montant, String mode, String reference) {
        Facture f = findEntityById(id);
        if (f.getStatut() == StatutFacture.ANNULEE)
            throw new BusinessRuleException("Impossible de payer une facture annulée.");
        if (f.getStatut() == StatutFacture.PAYEE)
            throw new BusinessRuleException("Cette facture est déjà entièrement payée.");
        if (f.getStatut() == StatutFacture.BROUILLON)
            throw new BusinessRuleException("Impossible de payer une facture en BROUILLON. Veuillez l'émettre.");

        double nouveauPaye = f.getMontantPaye() + montant;
        if (nouveauPaye > f.getMontantTotal())
            throw new BusinessRuleException(
                    "Le montant payé dépasse le total de la facture.");

        f.setMontantPaye(nouveauPaye);

        if (nouveauPaye >= f.getMontantTotal()) {
            f.setStatut(StatutFacture.PAYEE);
        } else {
            f.setStatut(StatutFacture.PARTIELLEMENT_PAYEE);
        }
        factureRepository.save(f);

        Paiement paiement = Paiement.builder()
                .facture(f)
                .montant(montant)
                .mode(mode)
                .reference(reference)
                .build();
        paiement = paiementRepository.save(paiement);

        return PaiementResponseDTO.builder()
                .id(paiement.getId())
                .montant(paiement.getMontant())
                .mode(paiement.getMode())
                .reference(paiement.getReference())
                .createdAt(paiement.getCreatedAt())
                .build();
    }

    @Override
    public List<PaiementResponseDTO> getPaiements(Long id) {
        return paiementRepository.findByFacture_Id(id).stream()
                .map(p -> PaiementResponseDTO.builder()
                        .id(p.getId())
                        .montant(p.getMontant())
                        .mode(p.getMode())
                        .reference(p.getReference())
                        .createdAt(p.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FactureResponseDTO annuler(Long id, String motif) {
        Facture f = findEntityById(id);
        if (f.getStatut() == StatutFacture.PAYEE)
            throw new BusinessRuleException("Une facture entièrement payée ne peut pas être annulée.");
        f.setStatut(StatutFacture.ANNULEE);
        f.setMotifAnnulation(motif);
        return factureMapper.toResponseDTO(factureRepository.save(f));
    }

    @Override
    public byte[] genererPdf(Long id) {
        Facture f = findEntityById(id);
        // Stub for generating PDF
        String pdfContent = "FACTURE PDF " + f.getNumero();
        return pdfContent.getBytes();
    }

    private Facture findEntityById(Long id) {
        return factureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable : " + id));
    }

    private String genererNumero() {
        int annee = Year.now().getValue();
        long count = factureRepository.count() + 1;
        return String.format("FAC-%d-%04d", annee, count);
    }
}
