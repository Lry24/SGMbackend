package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.entity.Document;
import com.sgm.SGMbackend.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Gestion des Documents", description = "Endpoints pour le stockage et la récupération des documents PDF")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT','MEDECIN')")
    public ResponseEntity<Document> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("typeDocument") String typeDocument,
            @RequestParam("entiteType") String entiteType,
            @RequestParam("entiteId") Long entiteId) {

        return ResponseEntity.status(201)
                .body(documentService.upload(file, typeDocument, entiteType, entiteId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT','MEDECIN')")
    public ResponseEntity<Document> getById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.findById(id));
    }

    @GetMapping("/{id}/telecharger")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT','MEDECIN')")
    public ResponseEntity<Map<String, String>> telecharger(@PathVariable Long id) {
        String url = documentService.genererLienTelecharge(id);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        documentService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/entite/{type}/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT','MEDECIN')")
    public ResponseEntity<List<Document>> parEntite(
            @PathVariable String type,
            @PathVariable Long id) {
        return ResponseEntity.ok(documentService.findByEntite(type, id));
    }
}
