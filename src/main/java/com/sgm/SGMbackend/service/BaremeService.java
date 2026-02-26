package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.dto.dtoRequest.BaremeRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.BaremeResponseDTO;

import java.util.List;

public interface BaremeService {

    List<BaremeResponseDTO> getActifs();

    BaremeResponseDTO creer(BaremeRequestDTO dto);

    BaremeResponseDTO modifier(Long id, BaremeRequestDTO dto);

    void desactiver(Long id);
}
