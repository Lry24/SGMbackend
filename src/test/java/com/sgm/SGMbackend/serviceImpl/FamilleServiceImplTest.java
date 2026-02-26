package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.dto.dtoRequest.FamilleRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.FamilleResponseDTO;
import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.Famille;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.mapper.FamilleMapper;
import com.sgm.SGMbackend.repository.DepouilleRepository;
import com.sgm.SGMbackend.repository.FamilleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FamilleServiceImplTest {

    @Mock
    private FamilleRepository familleRepository;

    @Mock
    private DepouilleRepository depouilleRepository;

    @Mock
    private FamilleMapper familleMapper;

    @InjectMocks
    private FamilleServiceImpl familleService;

    @Test
    @DisplayName("create: Succès")
    void create_Success() {
        // Arrange
        FamilleRequestDTO dto = new FamilleRequestDTO();
        dto.setTelephone("123456");
        Famille f = new Famille();
        FamilleResponseDTO res = new FamilleResponseDTO();

        when(familleRepository.existsByTelephone(dto.getTelephone())).thenReturn(false);
        when(familleMapper.toEntity(dto)).thenReturn(f);
        when(familleRepository.save(any(Famille.class))).thenReturn(f);
        when(familleMapper.toResponseDTO(f)).thenReturn(res);

        // Act
        FamilleResponseDTO result = familleService.create(dto);

        // Assert
        assertNotNull(result);
        assertTrue(f.getActif());
        verify(familleRepository).save(f);
    }

    @Test
    @DisplayName("create: Échec (Téléphone déjà utilisé)")
    void create_Failure_DuplicatePhone() {
        // Arrange
        FamilleRequestDTO dto = new FamilleRequestDTO();
        dto.setTelephone("123456");
        when(familleRepository.existsByTelephone(dto.getTelephone())).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> familleService.create(dto));
    }

    @Test
    @DisplayName("update: Succès")
    void update_Success() {
        // Arrange
        Long id = 1L;
        FamilleRequestDTO dto = new FamilleRequestDTO();
        dto.setTelephone("654321");
        Famille f = new Famille();
        f.setTelephone("123456");

        when(familleRepository.findById(id)).thenReturn(Optional.of(f));
        when(familleRepository.existsByTelephone(dto.getTelephone())).thenReturn(false);
        when(familleRepository.save(any(Famille.class))).thenReturn(f);
        when(familleMapper.toResponseDTO(f)).thenReturn(new FamilleResponseDTO());

        // Act
        familleService.update(id, dto);

        // Assert
        verify(familleMapper).updateEntity(dto, f);
        verify(familleRepository).save(f);
    }

    @Test
    @DisplayName("delete: Succès (Soft delete)")
    void delete_Success() {
        // Arrange
        Long id = 1L;
        Famille f = new Famille();
        f.setActif(true);
        when(familleRepository.findById(id)).thenReturn(Optional.of(f));

        // Act
        familleService.delete(id);

        // Assert
        assertFalse(f.getActif());
        verify(familleRepository).save(f);
    }

    @Test
    @DisplayName("lierDepouille: Succès")
    void lierDepouille_Success() {
        // Arrange
        Long fId = 1L;
        Long dId = 2L;
        Famille f = Famille.builder().id(fId).build();
        Depouille d = Depouille.builder().id(dId).build();

        when(familleRepository.findById(fId)).thenReturn(Optional.of(f));
        when(depouilleRepository.findById(dId)).thenReturn(Optional.of(d));

        // Act
        familleService.lierDepouille(fId, dId);

        // Assert
        assertEquals(f, d.getFamille());
        verify(depouilleRepository).save(d);
    }

    @Test
    @DisplayName("lierDepouille: Échec (Déjà liée)")
    void lierDepouille_Failure_AlreadyLinked() {
        // Arrange
        Long fId = 1L;
        Long otherFId = 9L;
        Long dId = 2L;
        Famille otherF = Famille.builder().id(otherFId).build();
        Depouille d = Depouille.builder().id(dId).famille(otherF).build();

        when(familleRepository.findById(fId)).thenReturn(Optional.of(new Famille()));
        when(depouilleRepository.findById(dId)).thenReturn(Optional.of(d));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> familleService.lierDepouille(fId, dId));
    }
}
