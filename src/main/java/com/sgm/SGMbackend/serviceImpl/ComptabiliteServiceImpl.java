package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.entity.Caisse;
import com.sgm.SGMbackend.entity.MouvementCaisse;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.repository.CaisseRepository;
import com.sgm.SGMbackend.repository.MouvementCaisseRepository;
import com.sgm.SGMbackend.service.ComptabiliteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class ComptabiliteServiceImpl implements ComptabiliteService {

    private final CaisseRepository caisseRepository;
    private final MouvementCaisseRepository mouvementRepository;

    @Override
    @Transactional
    public Caisse ouvrirCaisse(Double fondCaisse) {
        if (caisseRepository.findFirstByStatutOrderByDateOuvertureDesc("OUVERTE").isPresent()) {
            throw new BusinessRuleException("Une caisse est déjà ouverte.");
        }

        Caisse caisse = Caisse.builder()
                .dateOuverture(LocalDateTime.now())
                .fondCaisse(fondCaisse)
                .statut("OUVERTE")
                .build();

        return caisseRepository.save(caisse);
    }

    @Override
    @Transactional
    public Caisse fermerCaisse(Double soldeFinal) {
        Caisse caisse = caisseRepository.findFirstByStatutOrderByDateOuvertureDesc("OUVERTE")
                .orElseThrow(() -> new ResourceNotFoundException("Aucune caisse ouverte à fermer."));

        caisse.setDateFermeture(LocalDateTime.now());
        caisse.setSoldeFinal(soldeFinal);
        caisse.setStatut("FERMEE");

        return caisseRepository.save(caisse);
    }

    @Override
    public Page<MouvementCaisse> getJournal(LocalDateTime debut, LocalDateTime fin, Pageable pageable) {
        return mouvementRepository.findByDateBetween(debut, fin, pageable);
    }

    @Override
    public List<MouvementCaisse> getJournalList(LocalDateTime debut, LocalDateTime fin) {
        return mouvementRepository.findByDateBetween(debut, fin);
    }

    @Override
    public Map<String, Object> getCaisseJournaliere(LocalDateTime date) {
        LocalDateTime debut = date.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fin = date.withHour(23).withMinute(59).withSecond(59);

        List<MouvementCaisse> mouvements = mouvementRepository.findByDateBetween(debut, fin);

        double totalEncaissements = mouvements.stream()
                .filter(m -> "ENCAISSEMENT".equals(m.getType()))
                .mapToDouble(MouvementCaisse::getMontant).sum();

        double totalDecaissements = mouvements.stream()
                .filter(m -> "DECAISSEMENT".equals(m.getType()))
                .mapToDouble(MouvementCaisse::getMontant).sum();

        Map<String, Object> report = new HashMap<>();
        report.put("date", date);
        report.put("encaissements", totalEncaissements);
        report.put("decaissements", totalDecaissements);
        report.put("solde", totalEncaissements - totalDecaissements);
        report.put("mouvements", mouvements);

        return report;
    }

    @Override
    public Map<String, Double> getGrandLivre(String periode) {
        // Pour simplifier, on aggrège par mode de paiement sur tous les mouvements
        List<MouvementCaisse> mouvements = mouvementRepository.findAll();
        Map<String, Double> grandLivre = new HashMap<>();

        for (MouvementCaisse m : mouvements) {
            String cle = m.getModePaiement() != null ? m.getModePaiement() : "AUTRE";
            grandLivre.put(cle, grandLivre.getOrDefault(cle, 0.0) + m.getMontant());
        }
        return grandLivre;
    }

    @Override
    public Map<String, Double> getBalance(String periode) {
        List<MouvementCaisse> mouvements = mouvementRepository.findAll();
        double totalEncaisse = mouvements.stream()
                .filter(m -> "ENCAISSEMENT".equals(m.getType()))
                .mapToDouble(MouvementCaisse::getMontant).sum();

        double totalDecaisse = mouvements.stream()
                .filter(m -> "DECAISSEMENT".equals(m.getType()))
                .mapToDouble(MouvementCaisse::getMontant).sum();

        Map<String, Double> balance = new HashMap<>();
        balance.put("TOTAL_ENCAISSEMENTS", totalEncaisse);
        balance.put("TOTAL_DECAISSEMENTS", totalDecaisse);
        balance.put("SOLDE_NET", totalEncaisse - totalDecaisse);

        return balance;
    }

    @Override
    public byte[] exportJournal(LocalDateTime debut, LocalDateTime fin, String format) {
        List<MouvementCaisse> mouvements = mouvementRepository.findByDateBetween(debut, fin);

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Date,Libellé,Type,Montant,Mode,Facture\n");

        for (MouvementCaisse m : mouvements) {
            csv.append(m.getId()).append(",")
                    .append(m.getDate()).append(",")
                    .append("\"").append(m.getLibelle().replace("\"", "\"\"")).append("\",")
                    .append(m.getType()).append(",")
                    .append(m.getMontant()).append(",")
                    .append(m.getModePaiement()).append(",")
                    .append(m.getFacture() != null ? m.getFacture().getNumero() : "N/A")
                    .append("\n");
        }

        return csv.toString().getBytes();
    }
}
