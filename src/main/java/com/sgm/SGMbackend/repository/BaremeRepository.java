package com.sgm.SGMbackend.repository;

import com.sgm.SGMbackend.entity.Bareme;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BaremeRepository extends JpaRepository<Bareme, Long> {
    List<Bareme> findByActifTrue();
}
