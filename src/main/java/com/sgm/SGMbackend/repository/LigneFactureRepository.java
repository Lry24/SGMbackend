package com.sgm.SGMbackend.repository;

import com.sgm.SGMbackend.entity.LigneFacture;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LigneFactureRepository extends JpaRepository<LigneFacture, Long> {
    List<LigneFacture> findByFacture_Id(Long factureId);
}
