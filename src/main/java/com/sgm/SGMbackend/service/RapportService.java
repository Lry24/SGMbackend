package com.sgm.SGMbackend.service;

import java.time.LocalDate;
import java.util.Map;

public interface RapportService {
    Map<String, Object> getDashboardKpis();

    Map<String, Object> getActivite(LocalDate dateDebut, LocalDate dateFin, String format);

    Map<String, Object> getOccupation(LocalDate dateDebut, LocalDate dateFin);

    Map<String, Object> getFinancier(String periode, Integer annee, Integer mois);
}
