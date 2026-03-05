package com.sgm.SGMbackend.repository;

import com.sgm.SGMbackend.entity.MouvementDepouille;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MouvementDepouilleRepository extends JpaRepository<MouvementDepouille, Long> {
    List<MouvementDepouille> findByDepouilleIdOrderByDateMouvementDesc(Long depouilleId);
}
