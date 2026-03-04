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
    private final MouvementCaisseRepository mouvementRepo;
    private final AutopsieRepository autopsieRepo;

    @Override
    public Map<String, Object> getDashboardKpis() {
        Map<String, Object> kpis = new HashMap<>();
        kpis.put("totalDepouillesEnCours", depouilleRepo.countByStatutNot(StatutDepouille.RESTITUEE));
        kpis.put("alertesActives",
                alerteRepo.findByAcquitteeFalse(org.springframework.data.domain.Pageable.unpaged()).getTotalElements());

        long totalCapacite = chambreRepo.findAll().stream().mapToLong(c -> c.getCapacite()).sum();
        long totalOccupe = chambreRepo.findAll().stream()
                .flatMap(c -> c.getEmplacements().stream())
                .filter(e -> e.getOccupe())
                .count();

        Map<String, Object> occupation = new HashMap<>();
        occupation.put("total", totalCapacite);
        occupation.put("occupes", totalOccupe);
        occupation.put("taux", totalCapacite > 0 ? Math.round((float) totalOccupe / totalCapacite * 100) : 0);
        kpis.put("occupation", occupation);

        kpis.put("autopsiesPlanifiees", autopsieRepo.count());
        kpis.put("paiementsAttente",
                factureRepo.findByStatutIn(java.util.List.of(com.sgm.SGMbackend.entity.enums.StatutFacture.EMISE,
                        com.sgm.SGMbackend.entity.enums.StatutFacture.PARTIELLEMENT_PAYEE)).size());

        // Trends for charts
        LocalDateTime now = LocalDateTime.now();
        kpis.put("entreesTrend", depouilleRepo.countByDateArriveeBetweenGroupedByDate(now.minusDays(7), now));
        kpis.put("causesDistribution", depouilleRepo.countByCausePresumee());

        return kpis;
    }

    @Override
    public Map<String, Object> getActivite(LocalDate dateDebut, LocalDate dateFin, String format) {
        LocalDateTime start = dateDebut.atStartOfDay();
        LocalDateTime end = dateFin.atTime(23, 59, 59);

        Map<String, Object> data = new HashMap<>();
        data.put("entrees", depouilleRepo.countByDateArriveeBetweenGroupedByDate(start, end));
        data.put("total", depouilleRepo.countByDateArriveeBetween(start, end));
        data.put("periode", dateDebut.toString() + " au " + dateFin.toString());
        return data;
    }

    @Override
    public Map<String, Object> getOccupation(LocalDate dateDebut, LocalDate dateFin) {
        return getDashboardKpis(); // Simplified for now as it contains the same logic
    }

    @Override
    public Map<String, Object> getFinancier(String periode, Integer annee, Integer mois) {
        Map<String, Object> data = new HashMap<>();
        Double total = factureRepo.sumTotalRecettes();
        data.put("totalEncaisse", total != null ? total : 0.0);

        // Simulating some targets
        data.put("totalFacture", (total != null ? total : 0.0) * 1.2);
        data.put("tauxRecouvrement", 85);

        LocalDateTime now = LocalDateTime.now();
        data.put("recettesTrend", mouvementRepo.sumMontantByDateBetweenGroupedByDate(now.minusMonths(1), now));
        data.put("modesDistribution", mouvementRepo.countByModePaiement());

        // Dernières transactions (Top 5)
        data.put("dernieresTransactions", mouvementRepo.findAll(org.springframework.data.domain.PageRequest.of(0, 5,
                org.springframework.data.domain.Sort.by("date").descending())).getContent());

        return data;
    }
}
