package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.dto.dtoResponse.CaisseResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface ComptabiliteService {

    Page<Map<String, Object>> getJournal(String dateDebut, String dateFin, Pageable pageable);

    Map<String, Object> getGrandLivre(String periode);

    Map<String, Object> getBalance(String periode);

    CaisseResponseDTO getCaisseJour(String date);

    CaisseResponseDTO ouvrirCaisse(Double fondCaisse);

    CaisseResponseDTO fermerCaisse(Double soldeFinal);

    byte[] genererExport(String format, String dateDebut, String dateFin);
}
