package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import com.sgm.SGMbackend.repository.*;
import com.sgm.SGMbackend.service.RapportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RapportServiceImpl implements RapportService {

    private final DepouilleRepository depouilleRepo;
    private final ChambreFroideRepository chambreRepo;
    private final FactureRepository factureRepo;
    private final AlerteRepository alerteRepo;

    @Override
    public Map<String, Object> getDashboardKpis() {
        Map<String, Object> kpis = new HashMap<>();
        kpis.put("totalDepouillesEnCours", depouilleRepo.countByStatutNot(StatutDepouille.RESTITUEE));
        kpis.put("alertesActives",
                alerteRepo.findByAcquitteeFalse(org.springframework.data.domain.Pageable.unpaged()).getTotalElements());
        kpis.put("chambresOccupees", chambreRepo.findAll().stream()
                .filter(c -> c.getEmplacements().stream().anyMatch(e -> e.getOccupe())).count());
        kpis.put("totalChambres", chambreRepo.count());
        return kpis;
    }

    @Override
    public Map<String, Object> getActivite(LocalDate dateDebut, LocalDate dateFin, String format) {
        LocalDateTime start = dateDebut.atStartOfDay();
        LocalDateTime end = dateFin.atTime(23, 59, 59);

        Map<String, Object> data = new HashMap<>();
        data.put("entrees", depouilleRepo.countByDateArriveeBetween(start, end));
        data.put("periode", dateDebut.toString() + " au " + dateFin.toString());
        return data;
    }

    @Override
    public Map<String, Object> getOccupation(LocalDate dateDebut, LocalDate dateFin) {
        Map<String, Object> data = new HashMap<>();
        long totalCapacite = chambreRepo.findAll().stream().mapToLong(c -> c.getCapacite()).sum();
        long totalOccupe = chambreRepo.findAll().stream()
                .flatMap(c -> c.getEmplacements().stream())
                .filter(e -> e.getOccupe())
                .count();

        data.put("capaciteTotale", totalCapacite);
        data.put("occupationActuelle", totalOccupe);
        data.put("tauxOccupation", totalCapacite > 0 ? (float) totalOccupe / totalCapacite * 100 : 0);
        return data;
    }

    @Override
    public Map<String, Object> getFinancier(String periode, Integer annee, Integer mois) {
        Map<String, Object> data = new HashMap<>();
        Double total = factureRepo.sumTotalRecettes();
        data.put("totalRecettes", total != null ? total : 0.0);
        data.put("periode", periode);
        return data;
    }

}
