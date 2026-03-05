package com.sgm.SGMbackend.repository;

import com.sgm.SGMbackend.entity.Alerte;
import com.sgm.SGMbackend.entity.enums.TypeAlerte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlerteRepository extends JpaRepository<Alerte, Long> {
    Page<Alerte> findByAcquitteeFalse(Pageable pageable);

    Page<Alerte> findByTypeAndAcquitteeFalse(TypeAlerte type, Pageable pageable);

    List<Alerte> findByRoleDestinataireAndAcquitteeFalse(String role);
}
