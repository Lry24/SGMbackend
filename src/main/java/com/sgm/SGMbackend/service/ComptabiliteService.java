package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.entity.Caisse;
import com.sgm.SGMbackend.entity.MouvementCaisse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ComptabiliteService {
    Caisse ouvrirCaisse(Double fondCaisse);

    Caisse fermerCaisse(Double soldeFinal);

    Page<MouvementCaisse> getJournal(LocalDateTime debut, LocalDateTime fin, Pageable pageable);

    List<MouvementCaisse> getJournalList(LocalDateTime debut, LocalDateTime fin);

    Map<String, Object> getCaisseJournaliere(LocalDateTime date);

    Map<String, Double> getGrandLivre(String periode);

    Map<String, Double> getBalance(String periode);

    byte[] exportJournal(LocalDateTime debut, LocalDateTime fin, String format);
}
