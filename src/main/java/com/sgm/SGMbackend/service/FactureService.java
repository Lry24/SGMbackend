package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.entity.Facture;
import com.sgm.SGMbackend.entity.LigneFacture;
import java.util.List;

public interface FactureService {
    Facture creer(Long depouilleId, Long familleId, List<LigneFacture> lignes, Double remise);

    Facture enregistrerPaiement(Long id, Double montant, String mode, String reference);

    Facture emettre(Long id);

    Facture annuler(Long id, String motif);

    Facture findById(Long id);

    List<Facture> findAll();

    Facture findByDepouille(Long depouilleId);

    Double calculerEstimation(Long depouilleId);

    Facture modifier(Long id, List<LigneFacture> lignes, Double remise);

    List<com.sgm.SGMbackend.entity.MouvementCaisse> findPaiementsByFacture(Long id);

    byte[] generatePdf(Long id);
}
