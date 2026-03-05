package com.sgm.SGMbackend.repository;

import com.sgm.SGMbackend.entity.AlerteConfig;
import com.sgm.SGMbackend.entity.enums.TypeAlerte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlerteConfigRepository extends JpaRepository<AlerteConfig, Long> {
    Optional<AlerteConfig> findByType(TypeAlerte type);
}
