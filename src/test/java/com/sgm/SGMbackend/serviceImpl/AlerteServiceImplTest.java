package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.entity.Alerte;
import com.sgm.SGMbackend.entity.ChambreFroide;
import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.Emplacement;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import com.sgm.SGMbackend.entity.enums.TypeAlerte;
import com.sgm.SGMbackend.repository.AlerteConfigRepository;
import com.sgm.SGMbackend.repository.AlerteRepository;
import com.sgm.SGMbackend.repository.ChambreFroideRepository;
import com.sgm.SGMbackend.repository.DepouilleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlerteServiceImplTest {

    @Mock
    private AlerteRepository alerteRepo;
    @Mock
    private AlerteConfigRepository configRepo;
    @Mock
    private ChambreFroideRepository chambreRepo;
    @Mock
    private DepouilleRepository depouilleRepo;

    @InjectMocks
    private AlerteServiceImpl alerteService;

    @Test
    @DisplayName("verifierTemperature: Créer alerte si écart > 2°C")
    void verifierTemperature_ShouldCreateAlerte_WhenDeviationHigh() {
        // Act
        alerteService.verifierTemperature(1L, 5.0f, 2.0f); // Ecart 3.0 > 2.0

        // Assert
        verify(alerteRepo, times(1)).save(any(Alerte.class));
    }

    @Test
    @DisplayName("verifierTemperature: Pas d'alerte si écart <= 2°C")
    void verifierTemperature_ShouldNotCreateAlerte_WhenDeviationLow() {
        // Act
        alerteService.verifierTemperature(1L, 3.0f, 2.0f); // Ecart 1.0 <= 2.0

        // Assert
        verify(alerteRepo, never()).save(any(Alerte.class));
    }

    @Test
    @DisplayName("verifierSaturationChambres: Créer alerte si taux > 85%")
    void verifierSaturationChambres_ShouldCreateAlerte_WhenSaturationHigh() {
        // Arrange
        Emplacement e1 = Emplacement.builder().occupe(true).build();
        Emplacement e2 = Emplacement.builder().occupe(true).build();
        Emplacement e3 = Emplacement.builder().occupe(false).build();

        ChambreFroide cf = ChambreFroide.builder()
                .numero("C1")
                .capacite(2) // 2/2 = 100% saturation
                .emplacements(List.of(e1, e2))
                .build();

        when(chambreRepo.findAll()).thenReturn(List.of(cf));

        // Act
        alerteService.verifierSaturationChambres();

        // Assert
        verify(alerteRepo, times(1)).save(any(Alerte.class));
    }

    @Test
    @DisplayName("verifierDelaisReglementaires: Créer alerte si jours > 30")
    void verifierDelaisReglementaires_ShouldCreateAlerte_WhenOver30Days() {
        // Arrange
        Depouille d = Depouille.builder()
                .nomDefunt("Test")
                .statut(StatutDepouille.EN_CHAMBRE_FROIDE)
                .dateArrivee(LocalDateTime.now().minusDays(31))
                .build();

        when(depouilleRepo.findAll()).thenReturn(List.of(d));

        // Act
        alerteService.verifierDelaisReglementaires();

        // Assert
        verify(alerteRepo, times(1)).save(any(Alerte.class));
    }

    @Test
    @DisplayName("acquitter: Mettre à jour l'alerte")
    void acquitter_ShouldUpdateAlerte() {
        // Arrange
        Long id = 1L;
        Alerte a = Alerte.builder().id(id).acquittee(false).build();
        when(alerteRepo.findById(id)).thenReturn(java.util.Optional.of(a));

        // Act
        alerteService.acquitter(id, "OK");

        // Assert
        assertTrue(a.getAcquittee());
        assertNotNull(a.getDateAcquittement());
        assertEquals("OK", a.getCommentaireAcquittement());
        verify(alerteRepo).save(a);
    }

    private void assertTrue(boolean val) {
        if (!val)
            throw new AssertionError();
    }

    private void assertNotNull(Object val) {
        if (val == null)
            throw new AssertionError();
    }

    private void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual))
            throw new AssertionError();
    }
}
