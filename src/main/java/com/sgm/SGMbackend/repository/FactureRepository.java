package com.sgm.SGMbackend.repository;

import com.sgm.SGMbackend.entity.Facture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository pour l'entité Facture.
 * Stub — sera enrichi par DEV D (module Facturation).
 * La méthode findByDepouille_Id est utilisée par RestitutionService
 * pour vérifier que la facture est soldée avant de confirmer la restitution.
 */
public interface FactureRepository extends JpaRepository<Facture, Long> {

    Optional<Facture> findByDepouille_Id(Long depouillId);
}
