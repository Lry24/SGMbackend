package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.entity.ChambreFroide;
import com.sgm.SGMbackend.entity.Emplacement;
import com.sgm.SGMbackend.repository.ChambreFroideRepository;
import com.sgm.SGMbackend.repository.EmplacementRepository;
import com.sgm.SGMbackend.service.AlerteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChambreFroideServiceImplTest {

    @Mock
    private ChambreFroideRepository chambreRepo;
    @Mock
    private EmplacementRepository emplacementRepo;
    @Mock
    private AlerteService alerteService;

    @InjectMocks
    private ChambreFroideServiceImpl chambreService;

    @Test
    @DisplayName("creer: Générer automatiquement les emplacements")
    void creer_ShouldGenerateEmplacements() {
        // Arrange
        String numero = "C1";
        int capacite = 3;
        float temp = -4.0f;

        ChambreFroide savedChambre = ChambreFroide.builder().id(1L).numero(numero).capacite(capacite).build();
        when(chambreRepo.save(any(ChambreFroide.class))).thenReturn(savedChambre);

        // Act
        ChambreFroide result = chambreService.creer(numero, capacite, temp);

        // Assert
        assertNotNull(result);
        verify(chambreRepo).save(any(ChambreFroide.class));
        verify(emplacementRepo).saveAll(anyList());
        // Verify saveAll called with 3 emplacements
        verify(emplacementRepo).saveAll(argThat(iterable -> {
            int count = 0;
            for (Object obj : iterable)
                count++;
            return count == 3;
        }));
    }

    @Test
    @DisplayName("enregistrerTemperature: Mettre à jour et vérifier alerte")
    void enregistrerTemperature_ShouldUpdateAndCheckAlerte() {
        // Arrange
        Long id = 1L;
        ChambreFroide cf = ChambreFroide.builder().id(id).temperatureCible(-4.0f).build();
        when(chambreRepo.findById(id)).thenReturn(Optional.of(cf));

        // Act
        chambreService.enregistrerTemperature(id, -1.0f); // Ecart 3.0 > 2.0

        // Assert
        assertEquals(-1.0f, cf.getTemperatureActuelle());
        verify(chambreRepo).save(cf);
        verify(alerteService).verifierTemperature(eq(id), eq(-1.0f), eq(-4.0f));
    }
}
