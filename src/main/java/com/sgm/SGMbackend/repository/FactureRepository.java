package com.sgm.SGMbackend.repository;

import com.sgm.SGMbackend.entity.Facture;
import com.sgm.SGMbackend.entity.enums.StatutFacture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Facture.
 * La méthode findByDepouille_Id est utilisée par RestitutionService
 * pour vérifier que la facture est soldée avant de confirmer la restitution.
 */
public interface FactureRepository extends JpaRepository<Facture, Long> {

    Optional<Facture> findByDepouille_Id(Long depouillId);

    Page<Facture> findByStatutAndFamille_Id(StatutFacture statut, Long familleId, Pageable pageable);

    List<Facture> findByStatutIn(List<StatutFacture> statuts); // Impayées
}
