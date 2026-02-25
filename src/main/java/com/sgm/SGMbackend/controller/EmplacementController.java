package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.dto.dtoResponse.EmplacementResponseDTO;
import com.sgm.SGMbackend.entity.Emplacement;
import com.sgm.SGMbackend.mapper.EmplacementMapper;
import com.sgm.SGMbackend.service.EmplacementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/emplacements")
@RequiredArgsConstructor
public class EmplacementController {

    private final EmplacementService emplacementService;
    private final EmplacementMapper emplacementMapper;

    @GetMapping("/disponibles")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT')")
    public ResponseEntity<List<EmplacementResponseDTO>> findDisponibles() {
        return ResponseEntity.ok(emplacementService.findDisponibles().stream()
                .map(emplacementMapper::toResponseDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT')")
    public ResponseEntity<EmplacementResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(emplacementMapper.toResponseDTO(emplacementService.findById(id)));
    }

    @PostMapping("/affecter")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<EmplacementResponseDTO> affecter(@RequestBody Map<String, Long> body) {
        Emplacement emp = emplacementService.affecter(body.get("depouillId"), body.get("emplacementId"));
        return ResponseEntity.ok(emplacementMapper.toResponseDTO(emp));
    }

    @PatchMapping("/{id}/liberer")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<EmplacementResponseDTO> liberer(@PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Emplacement emp = emplacementService.liberer(id, body.get("motif"));
        return ResponseEntity.ok(emplacementMapper.toResponseDTO(emp));
    }
}
