package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.entity.Autopsie;
import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.enums.StatutAutopsie;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.repository.AutopsieRepository;
import com.sgm.SGMbackend.repository.DepouilleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutopsieServiceImplTest {

    @Mock
    private AutopsieRepository autoRepository;

    @Mock
    private DepouilleRepository depouilleRepository;

    @InjectMocks
    private AutopsieServiceImpl autopsieService;

    @Test
    @DisplayName("planifier: Succès")
    void planifier_Success() {
        // Arrange
        Long depId = 1L;
        String medId = "med-1";
        LocalDateTime date = LocalDateTime.now().plusDays(1);
        Depouille d = Depouille.builder().id(depId).statut(StatutDepouille.RECUE).build();

        when(depouilleRepository.findById(depId)).thenReturn(Optional.of(d));
        when(autoRepository.existsByDepouille_IdAndStatutIn(eq(depId), any())).thenReturn(false);
        when(autoRepository.save(any(Autopsie.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Autopsie result = autopsieService.planifier(depId, medId, date);

        // Assert
        assertNotNull(result);
        assertEquals(StatutAutopsie.PLANIFIEE, result.getStatut());
        assertEquals(StatutDepouille.EN_AUTOPSIE, d.getStatut());
        verify(depouilleRepository).save(d);
        verify(autoRepository).save(any(Autopsie.class));
    }

    @Test
    @DisplayName("planifier: Échec (Déjà une autopsie active)")
    void planifier_Failure_AlreadyActive() {
        // Arrange
        Long depId = 1L;
        Depouille d = Depouille.builder().id(depId).build();
        when(depouilleRepository.findById(depId)).thenReturn(Optional.of(d));
        when(autoRepository.existsByDepouille_IdAndStatutIn(eq(depId), any())).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> autopsieService.planifier(depId, "med", LocalDateTime.now()));
    }

    @Test
    @DisplayName("demarrer: Succès")
    void demarrer_Success() {
        // Arrange
        Long id = 1L;
        Autopsie a = Autopsie.builder().id(id).statut(StatutAutopsie.PLANIFIEE).build();
        when(autoRepository.findById(id)).thenReturn(Optional.of(a));
        when(autoRepository.save(any(Autopsie.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Autopsie result = autopsieService.demarrer(id);

        // Assert
        assertEquals(StatutAutopsie.EN_COURS, result.getStatut());
        assertNotNull(result.getDateDebut());
        verify(autoRepository).save(a);
    }

    @Test
    @DisplayName("terminer: Succès")
    void terminer_Success() {
        // Arrange
        Long id = 1L;
        Depouille d = Depouille.builder().statut(StatutDepouille.EN_AUTOPSIE).build();
        Autopsie a = Autopsie.builder().id(id).statut(StatutAutopsie.EN_COURS).depouille(d).build();

        when(autoRepository.findById(id)).thenReturn(Optional.of(a));
        when(autoRepository.save(any(Autopsie.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Autopsie result = autopsieService.terminer(id, "Rapport...", "Conclusion...");

        // Assert
        assertEquals(StatutAutopsie.TERMINEE, result.getStatut());
        assertEquals(StatutDepouille.PREPAREE, d.getStatut());
        assertNotNull(result.getDateFin());
        verify(depouilleRepository).save(d);
        verify(autoRepository).save(a);
    }

    @Test
    @DisplayName("ajouterAnalyse: Succès")
    void ajouterAnalyse_Success() {
        // Arrange
        Long id = 1L;
        Autopsie a = Autopsie.builder().id(id).analysesComplementaires("Base").build();
        when(autoRepository.findById(id)).thenReturn(Optional.of(a));
        when(autoRepository.save(any(Autopsie.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Autopsie result = autopsieService.ajouterAnalyse(id, "Extra");

        // Assert
        assertTrue(result.getAnalysesComplementaires().contains("Base"));
        assertTrue(result.getAnalysesComplementaires().contains("Extra"));
        verify(autoRepository).save(a);
    }

    @Test
    @DisplayName("annuler: Succès")
    void annuler_Success() {
        // Arrange
        Long id = 1L;
        Depouille d = Depouille.builder().statut(StatutDepouille.EN_AUTOPSIE).build();
        Autopsie a = Autopsie.builder().id(id).statut(StatutAutopsie.PLANIFIEE).depouille(d).build();
        when(autoRepository.findById(id)).thenReturn(Optional.of(a));

        // Act
        autopsieService.annuler(id);

        // Assert
        assertEquals(StatutDepouille.EN_CHAMBRE_FROIDE, d.getStatut());
        verify(depouilleRepository).save(d);
        verify(autoRepository).delete(a);
    }

    @Test
    @DisplayName("findById: Échec (Introuvable)")
    void findById_Failure_NotFound() {
        // Arrange
        when(autoRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> autopsieService.findById(1L));
    }
}
