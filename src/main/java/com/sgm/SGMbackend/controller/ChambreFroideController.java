package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.dto.dtoRequest.ChambreFroideRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.ChambreFroideResponseDTO;
import com.sgm.SGMbackend.entity.ChambreFroide;
import com.sgm.SGMbackend.mapper.ChambreFroideMapper;
import com.sgm.SGMbackend.service.ChambreFroideService;
import com.sgm.SGMbackend.dto.dtoResponse.EmplacementResponseDTO;
import com.sgm.SGMbackend.mapper.EmplacementMapper;
import com.sgm.SGMbackend.service.EmplacementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chambres")
@RequiredArgsConstructor
public class ChambreFroideController {

    private final ChambreFroideService chambreFroideService;
    private final ChambreFroideMapper chambreFroideMapper;
    private final EmplacementService emplacementService;
    private final EmplacementMapper emplacementMapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT')")
    public ResponseEntity<List<ChambreFroideResponseDTO>> findAll() {
        return ResponseEntity.ok(chambreFroideService.findAll().stream()
                .map(chambreFroideMapper::toResponseDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/cartographie")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT')")
    public ResponseEntity<List<ChambreFroideResponseDTO>> cartographie() {
        return ResponseEntity.ok(chambreFroideService.cartographie().stream()
                .map(chambreFroideMapper::toResponseDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT')")
    public ResponseEntity<ChambreFroideResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(chambreFroideMapper.toResponseDTO(chambreFroideService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChambreFroideResponseDTO> creer(@RequestBody @Valid ChambreFroideRequestDTO dto) {
        ChambreFroide chambre = chambreFroideService.creer(dto.getNumero(), dto.getCapacite(),
                dto.getTemperatureCible());
        return ResponseEntity.status(HttpStatus.CREATED).body(chambreFroideMapper.toResponseDTO(chambre));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChambreFroideResponseDTO> modifier(@PathVariable Long id,
            @RequestBody @Valid ChambreFroideRequestDTO dto) {
        ChambreFroide updated = chambreFroideService.modifier(id, dto.getCapacite(), dto.getTemperatureCible());
        return ResponseEntity.ok(chambreFroideMapper.toResponseDTO(updated));
    }

    @PatchMapping("/{id}/temperature")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<Void> enregistrerTemperature(@PathVariable Long id, @RequestBody Map<String, Float> body) {
        chambreFroideService.enregistrerTemperature(id, body.get("temperature"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/emplacements")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT')")
    public ResponseEntity<List<EmplacementResponseDTO>> getEmplacements(
            @PathVariable Long id,
            @RequestParam(required = false) Boolean occupe) {
        return ResponseEntity.ok(emplacementService.findByChambre(id, occupe).stream()
                .map(emplacementMapper::toResponseDTO)
                .collect(Collectors.toList()));
    }
}
