package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.entity.AuditLog;
import com.sgm.SGMbackend.entity.enums.GraviteAudit;
import com.sgm.SGMbackend.repository.AuditLogRepository;
import com.sgm.SGMbackend.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void log(String utilisateur, String action, String module, String details, GraviteAudit gravite) {
        AuditLog log = AuditLog.builder()
                .utilisateur(utilisateur)
                .action(action)
                .module(module)
                .details(details)
                .gravite(gravite)
                .date(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }

    @Override
    public Page<AuditLog> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    @Override
    public Page<AuditLog> searchLogs(String query, Pageable pageable) {
        return auditLogRepository
                .findByUtilisateurContainingIgnoreCaseOrActionContainingIgnoreCaseOrDetailsContainingIgnoreCase(
                        query, query, query, pageable);
    }
}
