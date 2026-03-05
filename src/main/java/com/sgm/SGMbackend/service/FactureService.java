package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.entity.Facture;
import com.sgm.SGMbackend.entity.LigneFacture;
import com.sgm.SGMbackend.entity.MouvementCaisse;
import com.sgm.SGMbackend.entity.enums.StatutFacture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface FactureService {
    Facture creer(Long depouilleId, Long familleId, List<LigneFacture> lignes, Double remise);

    Facture enregistrerPaiement(Long id, Double montant, String mode, String reference);

    Facture emettre(Long id);

    Facture annuler(Long id, String motif);

    Facture findById(Long id);

    Facture updateStatut(Long id, com.sgm.SGMbackend.entity.enums.StatutFacture statut);

    org.springframework.data.domain.Page<Facture> findAll(org.springframework.data.domain.Pageable pageable,
            com.sgm.SGMbackend.entity.enums.StatutFacture statut);

    Facture findByDepouille(Long depouilleId);

    Double calculerEstimation(Long depouilleId);

    Facture modifier(Long id, List<LigneFacture> lignes, Double remise);

    List<MouvementCaisse> findPaiementsByFacture(Long id);

    byte[] generatePdf(Long id);

    Facture findByNumero(String numero);
}
