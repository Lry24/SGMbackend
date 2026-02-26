package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.Facture;
import com.sgm.SGMbackend.entity.Famille;
import com.sgm.SGMbackend.entity.LigneFacture;
import com.sgm.SGMbackend.entity.enums.StatutFacture;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FactureServiceImplTest {

    @Mock
    private FactureRepository factureRepository;
    @Mock
    private LigneFactureRepository ligneRepository;
    @Mock
    private DepouilleRepository depouilleRepository;
    @Mock
    private FamilleRepository familleRepository;
    @Mock
    private MouvementCaisseRepository mouvementRepository;

    @InjectMocks
    private FactureServiceImpl factureService;

    @Test
    @DisplayName("creer: Succès avec calcul du montant total")
    void creer_Success() {
        // Arrange
        Long depId = 1L;
        Long famId = 2L;
        List<LigneFacture> lignes = List.of(
                LigneFacture.builder().quantite(2).prixUnitaire(5000.0).build(),
                LigneFacture.builder().quantite(1).prixUnitaire(10000.0).build());

        when(depouilleRepository.findById(depId)).thenReturn(Optional.of(new Depouille()));
        when(familleRepository.findById(famId)).thenReturn(Optional.of(new Famille()));
        when(factureRepository.save(any(Facture.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Facture result = factureService.creer(depId, famId, lignes, 2000.0);

        // Assert
        assertNotNull(result);
        assertEquals(18000.0, result.getMontantTotal()); // (2*5000 + 10000) - 2000
        assertEquals(StatutFacture.BROUILLON, result.getStatut());
        verify(ligneRepository).saveAll(anyList());
        verify(factureRepository).save(any(Facture.class));
    }

    @Test
    @DisplayName("enregistrerPaiement: Succès (Paiement partiel)")
    void enregistrerPaiement_Partial_Success() {
        // Arrange
        Long id = 1L;
        Facture f = Facture.builder().id(id).montantTotal(20000.0).montantPaye(5000.0).statut(StatutFacture.EMISE)
                .build();
        when(factureRepository.findById(id)).thenReturn(Optional.of(f));
        when(factureRepository.save(any(Facture.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Facture result = factureService.enregistrerPaiement(id, 5000.0, "ESPECES", "REF-123");

        // Assert
        assertEquals(10000.0, result.getMontantPaye());
        assertEquals(StatutFacture.PARTIELLEMENT_PAYEE, result.getStatut());
        verify(mouvementRepository).save(any());
        verify(factureRepository).save(f);
    }

    @Test
    @DisplayName("enregistrerPaiement: Succès (Paiement total)")
    void enregistrerPaiement_Full_Success() {
        // Arrange
        Long id = 1L;
        Facture f = Facture.builder().id(id).montantTotal(20000.0).montantPaye(15000.0).statut(StatutFacture.EMISE)
                .build();
        when(factureRepository.findById(id)).thenReturn(Optional.of(f));
        when(factureRepository.save(any(Facture.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Facture result = factureService.enregistrerPaiement(id, 5000.0, "ESPECES", "REF-123");

        // Assert
        assertEquals(20000.0, result.getMontantPaye());
        assertEquals(StatutFacture.PAYEE, result.getStatut());
    }

    @Test
    @DisplayName("enregistrerPaiement: Échec (Trop payé)")
    void enregistrerPaiement_Failure_TooMuch() {
        // Arrange
        Long id = 1L;
        Facture f = Facture.builder().id(id).montantTotal(10000.0).montantPaye(8000.0).build();
        when(factureRepository.findById(id)).thenReturn(Optional.of(f));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> factureService.enregistrerPaiement(id, 3000.0, "CASH", "XX"));
    }

    @Test
    @DisplayName("emettre: Succès")
    void emettre_Success() {
        // Arrange
        Long id = 1L;
        Facture f = Facture.builder().id(id).statut(StatutFacture.BROUILLON).build();
        when(factureRepository.findById(id)).thenReturn(Optional.of(f));
        when(factureRepository.save(any(Facture.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Facture result = factureService.emettre(id);

        // Assert
        assertEquals(StatutFacture.EMISE, result.getStatut());
    }

    @Test
    @DisplayName("annuler: Échec (Déjà payée)")
    void annuler_Failure_AlreadyPaid() {
        // Arrange
        Long id = 1L;
        Facture f = Facture.builder().id(id).statut(StatutFacture.PAYEE).build();
        when(factureRepository.findById(id)).thenReturn(Optional.of(f));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> factureService.annuler(id, "Erreur"));
    }
}
