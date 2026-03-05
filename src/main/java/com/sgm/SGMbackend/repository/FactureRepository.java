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
 * La méthode findByDepouilleId est utilisée par RestitutionService
 * pour vérifier que la facture est soldée avant de confirmer la restitution.
 */
public interface FactureRepository extends JpaRepository<Facture, Long> {

    List<Facture> findByDepouilleId(Long depouilleId);

    Optional<Facture> findByNumero(String numero);

    Page<Facture> findByStatut(StatutFacture statut, Pageable pageable);

    Page<Facture> findByStatutAndFamille_Id(StatutFacture statut, Long familleId, Pageable pageable);

    List<Facture> findByStatutIn(List<StatutFacture> statuts);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(f.montantPaye) FROM Facture f")
    Double sumTotalEncaisse();

    @org.springframework.data.jpa.repository.Query("SELECT SUM(f.montantTotal) FROM Facture f WHERE f.statut <> 'ANNULEE'")
    Double sumTotalFacture();

    @org.springframework.data.jpa.repository.Query("SELECT SUM(f.montantTotal) FROM Facture f WHERE f.statut = 'PAYEE'")
    Double sumTotalRecettes();
}
