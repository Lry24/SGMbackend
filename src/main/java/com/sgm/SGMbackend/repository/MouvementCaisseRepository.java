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
}
