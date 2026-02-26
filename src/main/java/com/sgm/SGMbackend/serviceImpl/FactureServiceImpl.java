package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.entity.Facture;
import com.sgm.SGMbackend.entity.LigneFacture;
import com.sgm.SGMbackend.entity.MouvementCaisse;
import com.sgm.SGMbackend.entity.enums.StatutFacture;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.repository.FactureRepository;
import com.sgm.SGMbackend.repository.LigneFactureRepository;
import com.sgm.SGMbackend.repository.DepouilleRepository;
import com.sgm.SGMbackend.repository.FamilleRepository;
import com.sgm.SGMbackend.repository.MouvementCaisseRepository;
import com.sgm.SGMbackend.service.FactureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FactureServiceImpl implements FactureService {

    private final FactureRepository factureRepository;
    private final LigneFactureRepository ligneRepository;
    private final DepouilleRepository depouilleRepository;
    private final FamilleRepository familleRepository;
    private final MouvementCaisseRepository mouvementRepository;

    @Override
    @Transactional
    public Facture creer(Long depouilleId, Long familleId, List<LigneFacture> lignes, Double remise) {
        var depouille = depouilleRepository.findById(depouilleId)
                .orElseThrow(() -> new ResourceNotFoundException("Dépouille introuvable"));

        var famille = familleRepository.findById(familleId)
                .orElseThrow(() -> new ResourceNotFoundException("Famille introuvable"));

        // Calculer le montant total
        double total = lignes.stream()
                .mapToDouble(l -> l.getQuantite() * l.getPrixUnitaire()).sum();

        double totalAvecRemise = total - (remise != null ? remise : 0.0);

        Facture f = Facture.builder()
                .numero(genererNumero())
                .depouille(depouille)
                .famille(famille)
                .montantTotal(totalAvecRemise)
                .montantPaye(0.0)
                .remise(remise != null ? remise : 0.0)
                .statut(StatutFacture.BROUILLON)
                .build();

        f = factureRepository.save(f);

        // Sauvegarder les lignes avec la référence à la facture
        final Facture factureFinal = f;
        lignes.forEach(l -> l.setFacture(factureFinal));
        ligneRepository.saveAll(lignes);

        return f;
    }

    @Override
    @Transactional
    public Facture enregistrerPaiement(Long id, Double montant, String mode, String reference) {
        Facture f = findById(id);

        if (f.getStatut() == StatutFacture.ANNULEE) {
            throw new BusinessRuleException("Impossible de payer une facture annulée.");
        }
        if (f.getStatut() == StatutFacture.PAYEE) {
            throw new BusinessRuleException("Cette facture est déjà entièrement payée.");
        }

        double nouveauPaye = (f.getMontantPaye() != null ? f.getMontantPaye() : 0.0) + montant;

        if (nouveauPaye > f.getMontantTotal()) {
            throw new BusinessRuleException("Le montant payé dépasse le total de la facture.");
        }

        f.setMontantPaye(nouveauPaye);

        // Mise à jour automatique du statut
        if (nouveauPaye >= f.getMontantTotal()) {
            f.setStatut(StatutFacture.PAYEE);
        } else {
            f.setStatut(StatutFacture.PARTIELLEMENT_PAYEE);
        }

        // Enregistrer le mouvement de caisse
        MouvementCaisse mv = MouvementCaisse.builder()
                .date(LocalDateTime.now())
                .montant(montant)
                .type("ENCAISSEMENT")
                .modePaiement(mode)
                .libelle("Paiement Facture " + f.getNumero() + " (" + reference + ")")
                .facture(f)
                .build();
        mouvementRepository.save(mv);

        return factureRepository.save(f);
    }

    @Override
    @Transactional
    public Facture emettre(Long id) {
        Facture f = findById(id);

        if (f.getStatut() != StatutFacture.BROUILLON) {
            throw new BusinessRuleException("Seul un BROUILLON peut être émis.");
        }

        f.setStatut(StatutFacture.EMISE);
        return factureRepository.save(f);
    }

    @Override
    @Transactional
    public Facture annuler(Long id, String motif) {
        Facture f = findById(id);

        if (f.getStatut() == StatutFacture.PAYEE) {
            throw new BusinessRuleException("Une facture entièrement payée ne peut pas être annulée.");
        }

        f.setStatut(StatutFacture.ANNULEE);
        f.setMotifAnnulation(motif);

        return factureRepository.save(f);
    }

    @Override
    public Facture findById(Long id) {
        return factureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable : " + id));
    }

    @Override
    public List<Facture> findAll() {
        return factureRepository.findAll();
    }

    @Override
    public Facture findByDepouille(Long depouilleId) {
        return factureRepository.findByDepouille_Id(depouilleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucune facture trouvée pour la dépouille : " + depouilleId));
    }

    @Override
    public Double calculerEstimation(Long depouilleId) {
        return 25000.0;
    }

    @Override
    @Transactional
    public Facture modifier(Long id, List<LigneFacture> lignes, Double remise) {
        Facture f = findById(id);
        if (f.getStatut() != StatutFacture.BROUILLON) {
            throw new BusinessRuleException("Seule une facture en BROUILLON peut être modifiée.");
        }

        // Supprimer anciennes lignes
        ligneRepository.deleteAll(f.getLignes());

        // Mettre à jour les lignes
        lignes.forEach(l -> l.setFacture(f));
        ligneRepository.saveAll(lignes);

        // Recalculer total
        double total = lignes.stream()
                .mapToDouble(l -> l.getQuantite() * l.getPrixUnitaire()).sum();
        double totalAvecRemise = total - (remise != null ? remise : 0.0);

        f.setLignes(lignes);
        f.setRemise(remise != null ? remise : 0.0);
        f.setMontantTotal(totalAvecRemise);

        return factureRepository.save(f);
    }

    @Override
    public List<MouvementCaisse> findPaiementsByFacture(Long id) {
        return mouvementRepository.findByFacture_Id(id);
    }

    @Override
    public byte[] generatePdf(Long id) {
        Facture f = findById(id);
        StringBuilder sb = new StringBuilder();
        sb.append("FACTURE NO: ").append(f.getNumero()).append("\n");
        sb.append("CLIENT: ").append(f.getFamille().getTuteurLegal()).append("\n");
        sb.append("TOTAL: ").append(f.getMontantTotal()).append("\n");
        sb.append("STATUT: ").append(f.getStatut()).append("\n");
        return sb.toString().getBytes();
    }

    private String genererNumero() {
        int annee = Year.now().getValue();
        long count = factureRepository.count() + 1;
        return String.format("FAC-%d-%04d", annee, count);
    }
}
