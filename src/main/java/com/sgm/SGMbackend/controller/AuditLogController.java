package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.entity.AuditLog;
import com.sgm.SGMbackend.service.AuditLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
@Tag(name = "Gestion de l'Audit", description = "Endpoints pour la consultation des journaux d'audit et logs système")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLog>> getLogs(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("date").descending());

        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(auditLogService.searchLogs(search, pageRequest));
        }
        return ResponseEntity.ok(auditLogService.getAllLogs(pageRequest));
    }
}
