package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.entity.enums.TypeAlerte;
import com.sgm.SGMbackend.dto.dtoResponse.AlerteResponseDTO;
import com.sgm.SGMbackend.dto.dtoResponse.AlerteConfigResponseDTO;
import com.sgm.SGMbackend.dto.dtoRequest.AlerteConfigRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AlerteService {
    void verifierTemperature(Long chambreId, float temperature, float temperatureCible);

    void verifierSaturationChambres();

    void verifierDelaisReglementaires();

    void creerAlerte(TypeAlerte type, String message, String roleDestinataire);

    Page<AlerteResponseDTO> findAll(TypeAlerte type, Pageable pageable);

    List<AlerteConfigResponseDTO> findAllConfigs();

    AlerteConfigResponseDTO saveConfig(AlerteConfigRequestDTO req);

    void acquitter(Long id, String commentaire);
}
