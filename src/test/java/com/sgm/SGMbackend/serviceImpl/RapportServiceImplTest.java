package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.entity.ChambreFroide;
import com.sgm.SGMbackend.entity.Emplacement;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import com.sgm.SGMbackend.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RapportServiceImplTest {

    @Mock
    private DepouilleRepository depouilleRepo;
    @Mock
    private ChambreFroideRepository chambreRepo;
    @Mock
    private FactureRepository factureRepo;
    @Mock
    private AlerteRepository alerteRepo;

    @InjectMocks
    private RapportServiceImpl rapportService;

    @Test
    @DisplayName("getDashboardKpis: Vérifier les compteurs")
    void getDashboardKpis_Verify() {
        // Arrange
        when(depouilleRepo.countByStatutNot(StatutDepouille.RESTITUEE)).thenReturn(10L);
        when(alerteRepo.findByAcquitteeFalse(any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));
        when(chambreRepo.count()).thenReturn(5L);

        // Act
        Map<String, Object> result = rapportService.getDashboardKpis();

        // Assert
        assertEquals(10L, result.get("totalDepouillesEnCours"));
        assertEquals(5L, result.get("totalChambres"));
    }

    @Test
    @DisplayName("getOccupation: Vérifier les calculs de taux")
    void getOccupation_VerifyCalculations() {
        // Arrange
        Emplacement e1 = Emplacement.builder().occupe(true).build();
        Emplacement e2 = Emplacement.builder().occupe(false).build();
        ChambreFroide cf = ChambreFroide.builder().capacite(2).emplacements(List.of(e1, e2)).build();
        when(chambreRepo.findAll()).thenReturn(List.of(cf));

        // Act
        Map<String, Object> result = rapportService.getOccupation(LocalDate.now(), LocalDate.now());

        // Assert
        assertEquals(2L, result.get("capaciteTotale"));
        assertEquals(1L, result.get("occupationActuelle"));
        assertEquals(50.0f, result.get("tauxOccupation"));
    }
}
