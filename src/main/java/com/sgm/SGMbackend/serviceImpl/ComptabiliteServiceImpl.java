package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.dto.dtoResponse.CaisseResponseDTO;
import com.sgm.SGMbackend.entity.Caisse;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.repository.CaisseRepository;
import com.sgm.SGMbackend.repository.PaiementRepository;
import com.sgm.SGMbackend.service.ComptabiliteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ComptabiliteServiceImpl implements ComptabiliteService {

    private final CaisseRepository caisseRepository;
    private final PaiementRepository paiementRepository; // si besoin de cumuler les paiements de la journée

    @Override
    public Page<Map<String, Object>> getJournal(String dateDebut, String dateFin, Pageable pageable) {
        // Stub for Journal comptable
        return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }

    @Override
    public Map<String, Object> getGrandLivre(String periode) {
        // Stub for Grand livre
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getBalance(String periode) {
        // Stub for Balance
        return new HashMap<>();
    }

    @Override
    public CaisseResponseDTO getCaisseJour(String dateParam) {
        LocalDate date = (dateParam != null && !dateParam.isEmpty()) ? LocalDate.parse(dateParam) : LocalDate.now();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        Caisse c = caisseRepository.findByDate(startOfDay, endOfDay)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune caisse ouverte pour cette date"));

        return toDto(c);
    }

    @Override
    @Transactional
    public CaisseResponseDTO ouvrirCaisse(Double fondCaisse) {
        LocalDate date = LocalDate.now();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        if (caisseRepository.findByDate(startOfDay, endOfDay).isPresent()) {
            throw new BusinessRuleException("La caisse est déjà ouverte pour aujourd'hui.");
        }

        Caisse c = Caisse.builder()
                .dateOuverture(LocalDateTime.now())
                .fondCaisse(fondCaisse)
                .estFermee(false)
                .build();
        return toDto(caisseRepository.save(c));
    }

    @Override
    @Transactional
    public CaisseResponseDTO fermerCaisse(Double soldeFinal) {
        Caisse c = caisseRepository.findTopByEstFermeeFalseOrderByDateOuvertureDesc()
                .orElseThrow(() -> new BusinessRuleException("Aucune caisse ouverte n'a été trouvée."));

        c.setDateFermeture(LocalDateTime.now());
        c.setSoldeFinal(soldeFinal);

        // En vrai: calculer c.getTotalEncaissements() via les paiements du jour
        c.setTotalEncaissements(0.0); // Stub

        c.setEcart(soldeFinal - (c.getFondCaisse() + c.getTotalEncaissements()));
        c.setEstFermee(true);

        return toDto(caisseRepository.save(c));
    }

    @Override
    public byte[] genererExport(String format, String dateDebut, String dateFin) {
        // Stub for CSV/Excel export
        String content = "Export " + format + " de " + dateDebut + " à " + dateFin;
        return content.getBytes();
    }

    private CaisseResponseDTO toDto(Caisse c) {
        return CaisseResponseDTO.builder()
                .id(c.getId())
                .dateOuverture(c.getDateOuverture())
                .dateFermeture(c.getDateFermeture())
                .fondCaisse(c.getFondCaisse())
                .soldeFinal(c.getSoldeFinal())
                .totalEncaissements(c.getTotalEncaissements())
                .ecart(c.getEcart())
                .estFermee(c.getEstFermee())
                .build();
    }
}
