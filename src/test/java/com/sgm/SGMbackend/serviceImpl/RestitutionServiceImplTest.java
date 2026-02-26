package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.dto.dtoRequest.RestitutionRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.RestitutionResponseDTO;
import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.Facture;
import com.sgm.SGMbackend.entity.Famille;
import com.sgm.SGMbackend.entity.Restitution;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import com.sgm.SGMbackend.entity.enums.StatutFacture;
import com.sgm.SGMbackend.entity.enums.StatutRestitution;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.mapper.FactureMapper;
import com.sgm.SGMbackend.mapper.RestitutionMapper;
import com.sgm.SGMbackend.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestitutionServiceImplTest {

    @Mock
    private RestitutionRepository restitutionRepository;
    @Mock
    private DepouilleRepository depouilleRepository;
    @Mock
    private FamilleRepository familleRepository;
    @Mock
    private FactureRepository factureRepository;
    @Mock
    private RestitutionMapper restitutionMapper;
    @Mock
    private FactureMapper factureMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RestitutionServiceImpl restitutionService;

    @Test
    @DisplayName("planifier: Succès")
    void planifier_Success() {
        // Arrange
        RestitutionRequestDTO dto = new RestitutionRequestDTO();
        dto.setDepouilleId(1L);
        dto.setFamilleId(2L);

        when(depouilleRepository.findById(1L)).thenReturn(Optional.of(new Depouille()));
        when(familleRepository.findById(2L)).thenReturn(Optional.of(new Famille()));
        Restitution r = new Restitution();
        when(restitutionMapper.toEntity(dto)).thenReturn(r);
        when(restitutionRepository.save(any())).thenReturn(r);

        // Act
        restitutionService.planifier(dto);

        // Assert
        assertEquals(StatutRestitution.PLANIFIEE, r.getStatut());
        verify(eventPublisher).publishEvent(any());
        verify(restitutionRepository).save(r);
    }

    @Test
    @DisplayName("confirmer: Succès")
    void confirmer_Success() {
        // Arrange
        Long id = 1L;
        Depouille d = Depouille.builder().id(10L).statut(StatutDepouille.PREPAREE).build();
        Restitution r = Restitution.builder().id(id).depouille(d).statut(StatutRestitution.PLANIFIEE).build();
        Facture f = Facture.builder().statut(StatutFacture.PAYEE).build();

        when(restitutionRepository.findById(id)).thenReturn(Optional.of(r));
        when(factureRepository.findByDepouille_Id(10L)).thenReturn(Optional.of(f));
        when(restitutionRepository.save(any())).thenReturn(r);

        // Act
        restitutionService.confirmer(id);

        // Assert
        assertEquals(StatutRestitution.CONFIRMEE, r.getStatut());
        assertTrue(r.getFacturesSoldees());
        verify(restitutionRepository).save(r);
    }

    @Test
    @DisplayName("confirmer: Échec (Facture non payée)")
    void confirmer_Failure_Unpaid() {
        // Arrange
        Long id = 1L;
        Depouille d = Depouille.builder().id(10L).statut(StatutDepouille.PREPAREE).build();
        Restitution r = Restitution.builder().id(id).depouille(d).statut(StatutRestitution.PLANIFIEE).build();
        Facture f = Facture.builder().statut(StatutFacture.EMISE).build();

        when(restitutionRepository.findById(id)).thenReturn(Optional.of(r));
        when(factureRepository.findByDepouille_Id(10L)).thenReturn(Optional.of(f));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> restitutionService.confirmer(id));
    }

    @Test
    @DisplayName("effectuer: Succès")
    void effectuer_Success() {
        // Arrange
        Long id = 1L;
        Depouille d = Depouille.builder().id(10L).statut(StatutDepouille.PREPAREE).build();
        Restitution r = Restitution.builder().id(id).depouille(d).statut(StatutRestitution.CONFIRMEE).build();

        when(restitutionRepository.findById(id)).thenReturn(Optional.of(r));
        when(restitutionRepository.save(any())).thenReturn(r);

        // Act
        restitutionService.effectuer(id, "PIECE-123");

        // Assert
        assertEquals(StatutRestitution.EFFECTUEE, r.getStatut());
        assertEquals(StatutDepouille.RESTITUEE, d.getStatut());
        assertNotNull(r.getDateEffective());
        verify(depouilleRepository).save(d);
        verify(restitutionRepository).save(r);
    }
}
