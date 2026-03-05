package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.entity.AuditLog;
import com.sgm.SGMbackend.entity.enums.GraviteAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {
    void log(String utilisateur, String action, String module, String details, GraviteAudit gravite);

    Page<AuditLog> getAllLogs(Pageable pageable);

    Page<AuditLog> searchLogs(String query, Pageable pageable);
}
