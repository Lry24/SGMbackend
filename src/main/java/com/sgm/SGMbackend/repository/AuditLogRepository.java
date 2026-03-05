package com.sgm.SGMbackend.repository;

import com.sgm.SGMbackend.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<AuditLog> findByUtilisateurContainingIgnoreCaseOrActionContainingIgnoreCaseOrDetailsContainingIgnoreCase(
            String utilisateur, String action, String details, Pageable pageable);
}
