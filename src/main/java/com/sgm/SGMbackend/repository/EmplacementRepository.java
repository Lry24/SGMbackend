
// EmplacementRepository.java
package com.sgm.SGMbackend.repository;
import com.sgm.SGMbackend.entity.Emplacement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface EmplacementRepository extends JpaRepository<Emplacement, Long> {
    List<Emplacement> findByChambreFroide_Id(Long chambreId);
    List<Emplacement> findByChambreFroide_IdAndOccupe(Long chambreId, Boolean occupe);
    List<Emplacement> findByOccupeFalse();  // Tous les emplacements libres
    long countByChambreFroide_IdAndOccupeTrue(Long chambreId);
}