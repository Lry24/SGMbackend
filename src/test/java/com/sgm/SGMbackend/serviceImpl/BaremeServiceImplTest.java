package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.entity.Bareme;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.repository.BaremeRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaremeServiceImplTest {

    @Mock
    private BaremeRepository baremeRepository;

    @InjectMocks
    private BaremeServiceImpl baremeService;

    @Test
    @DisplayName("creer: Succès")
    void creer_Success() {
        // Arrange
        Bareme b = Bareme.builder().nom("Cercueil").prix(50000.0).build();
        when(baremeRepository.save(any(Bareme.class))).thenReturn(b);

        // Act
        Bareme result = baremeService.creer(b);

        // Assert
        assertNotNull(result);
        assertTrue(result.getActif());
        verify(baremeRepository).save(b);
    }

    @Test
    @DisplayName("modifier: Succès")
    void modifier_Success() {
        // Arrange
        Long id = 1L;
        Bareme existing = Bareme.builder().id(id).nom("Ancien").prix(100.0).build();
        Bareme updated = Bareme.builder().nom("Nouveau").prix(200.0).actif(true).build();

        when(baremeRepository.findById(id)).thenReturn(Optional.of(existing));
        when(baremeRepository.save(any(Bareme.class))).thenReturn(existing);

        // Act
        Bareme result = baremeService.modifier(id, updated);

        // Assert
        assertEquals("Nouveau", result.getNom());
        assertEquals(200.0, result.getPrix());
        verify(baremeRepository).save(existing);
    }

    @Test
    @DisplayName("softDelete: Succès")
    void softDelete_Success() {
        // Arrange
        Long id = 1L;
        Bareme existing = Bareme.builder().id(id).actif(true).build();
        when(baremeRepository.findById(id)).thenReturn(Optional.of(existing));

        // Act
        baremeService.softDelete(id);

        // Assert
        assertFalse(existing.getActif());
        verify(baremeRepository).save(existing);
    }

    @Test
    @DisplayName("findAllActifs: Liste")
    void findAllActifs_Success() {
        // Arrange
        when(baremeRepository.findByActifTrue()).thenReturn(List.of(new Bareme()));

        // Act
        List<Bareme> result = baremeService.findAllActifs();

        // Assert
        assertEquals(1, result.size());
        verify(baremeRepository).findByActifTrue();
    }
}
