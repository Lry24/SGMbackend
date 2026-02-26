package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.Emplacement;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.repository.DepouilleRepository;
import com.sgm.SGMbackend.repository.EmplacementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmplacementServiceImplTest {

    @Mock
    private EmplacementRepository emplacementRepo;
    @Mock
    private DepouilleRepository depouilleRepo;

    @InjectMocks
    private EmplacementServiceImpl emplacementService;

    @Test
    @DisplayName("affecter: Succès")
    void affecter_Success() {
        // Arrange
        Long empId = 1L;
        Long depId = 2L;
        Emplacement emp = Emplacement.builder().id(empId).occupe(false).build();
        Depouille dep = Depouille.builder().id(depId).build();

        when(emplacementRepo.findById(empId)).thenReturn(Optional.of(emp));
        when(depouilleRepo.findById(depId)).thenReturn(Optional.of(dep));
        when(emplacementRepo.save(any())).thenReturn(emp);

        // Act
        Emplacement result = emplacementService.affecter(depId, empId);

        // Assert
        assertTrue(result.getOccupe());
        assertEquals(dep, result.getDepouille());
        assertEquals(StatutDepouille.EN_CHAMBRE_FROIDE, dep.getStatut());
        verify(depouilleRepo).save(dep);
        verify(emplacementRepo).save(emp);
    }

    @Test
    @DisplayName("affecter: Échec si déjà occupé")
    void affecter_Failure_AlreadyOccupied() {
        // Arrange
        Long empId = 1L;
        Emplacement emp = Emplacement.builder().id(empId).occupe(true).build();
        when(emplacementRepo.findById(empId)).thenReturn(Optional.of(emp));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> emplacementService.affecter(2L, empId));
    }

    @Test
    @DisplayName("liberer: Succès")
    void liberer_Success() {
        // Arrange
        Long id = 1L;
        Depouille dep = new Depouille();
        Emplacement emp = Emplacement.builder().id(id).occupe(true).depouille(dep).build();
        when(emplacementRepo.findById(id)).thenReturn(Optional.of(emp));
        when(emplacementRepo.save(any())).thenReturn(emp);

        // Act
        Emplacement result = emplacementService.liberer(id, "Transfert");

        // Assert
        assertFalse(result.getOccupe());
        assertNull(result.getDepouille());
        assertNull(dep.getEmplacement());
        verify(depouilleRepo).save(dep);
        verify(emplacementRepo).save(emp);
    }
}
