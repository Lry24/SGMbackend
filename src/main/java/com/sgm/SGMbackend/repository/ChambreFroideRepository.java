// ChambreFroideRepository.java
package com.sgm.SGMbackend.repository;
import com.sgm.SGMbackend.entity.ChambreFroide;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ChambreFroideRepository extends JpaRepository<ChambreFroide, Long> {
    Optional<ChambreFroide> findByNumero(String numero);
}

