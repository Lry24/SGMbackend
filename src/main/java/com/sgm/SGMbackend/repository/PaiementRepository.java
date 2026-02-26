package com.sgm.SGMbackend.repository;

import com.sgm.SGMbackend.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    List<Paiement> findByFacture_Id(Long factureId);
}
