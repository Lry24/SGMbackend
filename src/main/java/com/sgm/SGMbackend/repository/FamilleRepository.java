package com.sgm.SGMbackend.repository;

import com.sgm.SGMbackend.entity.Famille;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FamilleRepository extends JpaRepository<Famille, Long> {

    @Query("SELECT f FROM Famille f WHERE f.actif = true AND (" +
            "LOWER(f.tuteurLegal) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "f.telephone LIKE CONCAT('%',:q,'%'))")
    Page<Famille> rechercherActives(@Param("q") String q, Pageable pageable);

    boolean existsByTelephone(String telephone);
}