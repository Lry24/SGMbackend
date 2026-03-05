package com.sgm.SGMbackend.repository;

import com.sgm.SGMbackend.entity.MouvementCaisse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface MouvementCaisseRepository extends JpaRepository<MouvementCaisse, Long> {
    Page<MouvementCaisse> findByDateBetween(LocalDateTime debut, LocalDateTime fin, Pageable pageable);

    List<MouvementCaisse> findByDateBetween(LocalDateTime debut, LocalDateTime fin);

    List<MouvementCaisse> findByFacture_Id(Long factureId);

    @org.springframework.data.jpa.repository.Query("SELECT m.modePaiement, COUNT(m) FROM MouvementCaisse m WHERE m.type = 'ENCAISSEMENT' GROUP BY m.modePaiement")
    List<Object[]> countByModePaiement();

    @org.springframework.data.jpa.repository.Query("SELECT CAST(m.date AS date), SUM(m.montant) FROM MouvementCaisse m WHERE m.type = 'ENCAISSEMENT' AND m.date BETWEEN :debut AND :fin GROUP BY CAST(m.date AS date) ORDER BY CAST(m.date AS date)")
    List<Object[]> sumMontantByDateBetweenGroupedByDate(
            @org.springframework.data.repository.query.Param("debut") LocalDateTime debut,
            @org.springframework.data.repository.query.Param("fin") LocalDateTime fin);

    long countByDateBetweenAndType(LocalDateTime debut, LocalDateTime fin, String type);
}
