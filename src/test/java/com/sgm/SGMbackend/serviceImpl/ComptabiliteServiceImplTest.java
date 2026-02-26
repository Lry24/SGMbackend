package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.entity.Caisse;
import com.sgm.SGMbackend.entity.MouvementCaisse;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.repository.CaisseRepository;
import com.sgm.SGMbackend.repository.MouvementCaisseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComptabiliteServiceImplTest {

    @Mock
    private CaisseRepository caisseRepository;

    @Mock
    private MouvementCaisseRepository mouvementRepository;

    @InjectMocks
    private ComptabiliteServiceImpl comptabiliteService;

    @Test
    @DisplayName("ouvrirCaisse: Succès")
    void ouvrirCaisse_Success() {
        // Arrange
        when(caisseRepository.findFirstByStatutOrderByDateOuvertureDesc("OUVERTE")).thenReturn(Optional.empty());
        when(caisseRepository.save(any(Caisse.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Caisse result = comptabiliteService.ouvrirCaisse(5000.0);

        // Assert
        assertNotNull(result);
        assertEquals(5000.0, result.getFondCaisse());
        assertEquals("OUVERTE", result.getStatut());
        verify(caisseRepository).save(any(Caisse.class));
    }

    @Test
    @DisplayName("ouvrirCaisse: Échec si déjà ouverte")
    void ouvrirCaisse_Failure_AlreadyOpen() {
        // Arrange
        when(caisseRepository.findFirstByStatutOrderByDateOuvertureDesc("OUVERTE"))
                .thenReturn(Optional.of(new Caisse()));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> comptabiliteService.ouvrirCaisse(100.0));
    }

    @Test
    @DisplayName("fermerCaisse: Succès")
    void fermerCaisse_Success() {
        // Arrange
        Caisse caisse = Caisse.builder().statut("OUVERTE").build();
        when(caisseRepository.findFirstByStatutOrderByDateOuvertureDesc("OUVERTE")).thenReturn(Optional.of(caisse));
        when(caisseRepository.save(any(Caisse.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Caisse result = comptabiliteService.fermerCaisse(15000.0);

        // Assert
        assertEquals("FERMEE", result.getStatut());
        assertEquals(15000.0, result.getSoldeFinal());
        assertNotNull(result.getDateFermeture());
    }

    @Test
    @DisplayName("getCaisseJournaliere: Vérifier calculs")
    void getCaisseJournaliere_Calculations() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<MouvementCaisse> mouvements = List.of(
                MouvementCaisse.builder().montant(1000.0).type("ENCAISSEMENT").build(),
                MouvementCaisse.builder().montant(200.0).type("DECAISSEMENT").build());
        when(mouvementRepository.findByDateBetween(any(), any())).thenReturn(mouvements);

        // Act
        Map<String, Object> result = comptabiliteService.getCaisseJournaliere(now);

        // Assert
        assertEquals(1000.0, result.get("encaissements"));
        assertEquals(200.0, result.get("decaissements"));
        assertEquals(800.0, result.get("solde"));
    }

    @Test
    @DisplayName("exportJournal: Vérifier format CSV")
    void exportJournal_CsvFormat() {
        // Arrange
        MouvementCaisse m = MouvementCaisse.builder()
                .id(1L)
                .libelle("Test")
                .type("ENCAISSEMENT")
                .montant(500.0)
                .modePaiement("CASH")
                .build();
        when(mouvementRepository.findByDateBetween(any(), any())).thenReturn(List.of(m));

        // Act
        byte[] result = comptabiliteService.exportJournal(LocalDateTime.now(), LocalDateTime.now(), "CSV");

        // Assert
        String content = new String(result);
        assertTrue(content.contains("ID,Date,Libellé,Type,Montant,Mode,Facture"));
        assertTrue(content.contains("Test"));
        assertTrue(content.contains("500.0"));
    }
}
