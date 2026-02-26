package com.sgm.SGMbackend.repository;

import com.sgm.SGMbackend.entity.Caisse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CaisseRepository extends JpaRepository<Caisse, Long> {
    Optional<Caisse> findFirstByStatutOrderByDateOuvertureDesc(String statut);
}
