package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.repository.DepouilleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepouilleServiceImplTest {

    @Mock
    private DepouilleRepository depouilleRepository;

    @InjectMocks
    private DepouilleServiceImpl depouilleService;

    @Test
    @DisplayName("enregistrer: Succès et génération ID unique")
    void enregistrer_Success() {
        // Arrange
        Depouille d = Depouille.builder().nomDefunt("Doe").build();
        when(depouilleRepository.count()).thenReturn(10L);
        when(depouilleRepository.save(any(Depouille.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Depouille result = depouilleService.enregistrer(d);

        // Assert
        assertNotNull(result.getIdentifiantUnique());
        assertTrue(result.getIdentifiantUnique().startsWith("SGM-2026-"));
        assertEquals(StatutDepouille.RECUE, result.getStatut());
        verify(depouilleRepository).save(d);
    }

    @Test
    @DisplayName("changerStatut: Succès (RECUE -> EN_CHAMBRE_FROIDE)")
    void changerStatut_Success_RecueToChambre() {
        // Arrange
        Long id = 1L;
        Depouille d = Depouille.builder().id(id).statut(StatutDepouille.RECUE).build();
        when(depouilleRepository.findById(id)).thenReturn(Optional.of(d));
        when(depouilleRepository.save(any(Depouille.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Depouille result = depouilleService.changerStatut(id, StatutDepouille.EN_CHAMBRE_FROIDE);

        // Assert
        assertEquals(StatutDepouille.EN_CHAMBRE_FROIDE, result.getStatut());
        verify(depouilleRepository).save(d);
    }

    @Test
    @DisplayName("changerStatut: Échec (Transition invalide)")
    void changerStatut_Failure_InvalidTransition() {
        // Arrange
        Long id = 1L;
        Depouille d = Depouille.builder().id(id).statut(StatutDepouille.RECUE).build();
        when(depouilleRepository.findById(id)).thenReturn(Optional.of(d));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> depouilleService.changerStatut(id, StatutDepouille.RESTITUEE));
    }

    @Test
    @DisplayName("supprimer: Succès (Statut == RECUE)")
    void supprimer_Success() {
        // Arrange
        Long id = 1L;
        Depouille d = Depouille.builder().id(id).statut(StatutDepouille.RECUE).build();
        when(depouilleRepository.findById(id)).thenReturn(Optional.of(d));

        // Act
        depouilleService.supprimer(id);

        // Assert
        verify(depouilleRepository).delete(d);
    }

    @Test
    @DisplayName("supprimer: Échec (Statut != RECUE)")
    void supprimer_Failure_NotRecue() {
        // Arrange
        Long id = 1L;
        Depouille d = Depouille.builder().id(id).statut(StatutDepouille.EN_CHAMBRE_FROIDE).build();
        when(depouilleRepository.findById(id)).thenReturn(Optional.of(d));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> depouilleService.supprimer(id));
    }

    @Test
    @DisplayName("findAll: Liste paginée")
    void findAll_Success() {
        // Arrange
        Page<Depouille> page = new PageImpl<>(List.of(new Depouille()));
        when(depouilleRepository.findAll(any(Pageable.class))).thenReturn(page);

        // Act
        Page<Depouille> result = depouilleService.findAll(Pageable.unpaged(), null, null);

        // Assert
        assertEquals(1, result.getSize());
        verify(depouilleRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("getQRCode: Succès")
    void getQRCode_Success() {
        // Arrange
        Long id = 1L;
        Depouille d = Depouille.builder().id(id).identifiantUnique("SGM-2026-00001").build();
        when(depouilleRepository.findById(id)).thenReturn(Optional.of(d));

        // Act
        byte[] qr = depouilleService.getQRCode(id);

        // Assert
        assertNotNull(qr);
        assertTrue(qr.length > 0);
    }
}
