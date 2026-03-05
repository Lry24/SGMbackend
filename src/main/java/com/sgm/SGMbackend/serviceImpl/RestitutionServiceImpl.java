package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.dto.dtoRequest.RestitutionRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.RestitutionResponseDTO;
import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.Facture;
import com.sgm.SGMbackend.entity.Famille;
import com.sgm.SGMbackend.entity.Restitution;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import com.sgm.SGMbackend.entity.enums.StatutFacture;
import com.sgm.SGMbackend.entity.enums.StatutRestitution;
import com.sgm.SGMbackend.entity.enums.GraviteAudit;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.dto.dtoResponse.FactureResponseDTO;
import com.sgm.SGMbackend.event.RestitutionPlanifieeEvent;
import com.sgm.SGMbackend.mapper.FactureMapper;
import com.sgm.SGMbackend.mapper.RestitutionMapper;
import com.sgm.SGMbackend.repository.*;
import com.sgm.SGMbackend.service.AuditLogService;
import com.sgm.SGMbackend.service.RestitutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestitutionServiceImpl implements RestitutionService {

        private final RestitutionRepository restitutionRepository;
        private final DepouilleRepository depouilleRepository;
        private final FamilleRepository familleRepository;
        private final FactureRepository factureRepository;
        private final FactureMapper factureMapper;
        private final ApplicationEventPublisher eventPublisher;
        private final AuditLogService auditLogService;
        private final RestitutionMapper restitutionMapper;

        @Override
        @Transactional
        public RestitutionResponseDTO planifier(RestitutionRequestDTO requestDTO) {
                Depouille depouille;
                Famille famille;

                // Si numeroFacture est fourni, on l'utilise pour trouver la dépouille et la
                // famille
                if (requestDTO.getNumeroFacture() != null && !requestDTO.getNumeroFacture().isBlank()) {
                        Facture f = factureRepository.findByNumero(requestDTO.getNumeroFacture())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Facture introuvable : " + requestDTO.getNumeroFacture()));
                        depouille = f.getDepouille();
                        famille = f.getFamille();

                        if (f.getStatut() != StatutFacture.PAYEE) {
                                throw new BusinessRuleException(
                                                "La planification n'est possible que si la facture est PAYEE.");
                        }
                } else {
                        // Sinon on utilise les IDs classiques
                        if (requestDTO.getDepouilleId() == null) {
                                throw new BusinessRuleException(
                                                "L'ID de la dépouille ou le numéro de facture est obligatoire.");
                        }
                        if (requestDTO.getFamilleId() == null) {
                                throw new BusinessRuleException(
                                                "L'ID de la famille ou le numéro de facture est obligatoire.");
                        }

                        depouille = depouilleRepository.findById(requestDTO.getDepouilleId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Dépouille introuvable : " + requestDTO.getDepouilleId()));

                        famille = familleRepository.findById(requestDTO.getFamilleId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Famille introuvable : " + requestDTO.getFamilleId()));

                        // Vérifier quand même que la facture de cette dépouille est payée
                        List<Facture> factures = factureRepository.findByDepouilleId(depouille.getId());
                        Facture f = factures.stream()
                                        .filter(fact -> fact.getStatut() != StatutFacture.ANNULEE)
                                        .max((f1, f2) -> f1.getId().compareTo(f2.getId()))
                                        .orElseThrow(() -> new BusinessRuleException(
                                                        "Aucune facture trouvée pour cette dépouille."));

                        if (f.getStatut() != StatutFacture.PAYEE) {
                                throw new BusinessRuleException(
                                                "La planification n'est possible que si la facture est PAYEE.");
                        }
                }

                Restitution restitution = restitutionMapper.toEntity(requestDTO);
                restitution.setDepouille(depouille);
                restitution.setFamille(famille);
                restitution.setStatut(StatutRestitution.PLANIFIEE);

                Restitution saved = restitutionRepository.save(restitution);
                eventPublisher.publishEvent(new RestitutionPlanifieeEvent(this, saved));

                auditLogService.log(
                                "RESPONSABLE",
                                "PLANIFICATION_RESTITUTION",
                                "MORGUE",
                                "Restitution planifiée pour la dépouille: " + depouille.getNomDefunt(),
                                GraviteAudit.INFO);

                return restitutionMapper.toResponseDTO(saved);
        }

        @Override
        @Transactional
        public RestitutionResponseDTO confirmer(Long id) {
                Restitution r = restitutionRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Restitution introuvable : " + id));

                if (r.getStatut() != StatutRestitution.PLANIFIEE) {
                        throw new BusinessRuleException("Seule une restitution PLANIFIEE peut être confirmée.");
                }

                // Règle 1 : Vérifier la facture (doit être PAYEE)
                List<Facture> factures = factureRepository.findByDepouilleId(r.getDepouille().getId());
                Facture factureValide = factures.stream()
                                .filter(f -> f.getStatut() != StatutFacture.ANNULEE)
                                .max((f1, f2) -> f1.getId().compareTo(f2.getId()))
                                .orElse(null);

                if (factureValide == null) {
                        throw new BusinessRuleException("Aucune facture trouvée pour cette dépouille.");
                }
                if (factureValide.getStatut() != StatutFacture.PAYEE) {
                        throw new BusinessRuleException("La facture associée doit être PAYEE avant confirmation.");
                }

                // Règle 2 : Statut dépouille (PREPAREE ou EN_CHAMBRE_FROIDE)
                StatutDepouille currentStatut = r.getDepouille().getStatut();
                if (currentStatut != StatutDepouille.PREPAREE && currentStatut != StatutDepouille.EN_CHAMBRE_FROIDE) {
                        throw new BusinessRuleException(
                                        "La dépouille n'est pas dans un état permettant la restitution (actuel: "
                                                        + currentStatut + ").");
                }

                r.setFacturesSoldees(true);
                r.setDocumentsComplets(true); // À affiner si besoin de vérifier l'entité Document
                r.setStatut(StatutRestitution.CONFIRMEE);

                Restitution saved = restitutionRepository.save(r);

                auditLogService.log(
                                "RESPONSABLE",
                                "CONFIRMATION_RESTITUTION",
                                "MORGUE",
                                "Restitution confirmée pour la dépouille: " + r.getDepouille().getNomDefunt(),
                                GraviteAudit.INFO);

                return restitutionMapper.toResponseDTO(saved);
        }

        @Override
        @Transactional
        public void annuler(Long id, String motif) {
                Restitution r = restitutionRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Restitution introuvable : " + id));

                r.setStatut(StatutRestitution.ANNULEE);
                r.setMotifAnnulation(motif);
                restitutionRepository.save(r);

                auditLogService.log(
                                "RESPONSABLE",
                                "ANNULATION_RESTITUTION",
                                "MORGUE",
                                "Restitution annulée pour: " + r.getDepouille().getNomDefunt() + ". Motif: " + motif,
                                GraviteAudit.WARNING);
        }

        @Override
        @Transactional
        public RestitutionResponseDTO effectuer(Long id, String pieceIdentiteRef) {
                Restitution r = restitutionRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Restitution introuvable : " + id));

                if (r.getStatut() != StatutRestitution.CONFIRMEE) {
                        throw new BusinessRuleException("La restitution doit être CONFIRMEE avant d'être effectuée.");
                }

                r.setPieceIdentiteRef(pieceIdentiteRef);
                r.setDateEffective(LocalDateTime.now());
                r.setStatut(StatutRestitution.EFFECTUEE);

                // Mettre à jour la dépouille
                Depouille dep = r.getDepouille();
                dep.setStatut(StatutDepouille.RESTITUEE);

                // Libérer l'emplacement
                if (dep.getEmplacement() != null) {
                        dep.getEmplacement().setOccupe(false);
                        dep.setEmplacement(null);
                }

                depouilleRepository.save(dep);
                Restitution saved = restitutionRepository.save(r);

                auditLogService.log(
                                "AGENT",
                                "EXÉCUTION_RESTITUTION",
                                "MORGUE",
                                "Restitution effectuée pour: " + saved.getDepouille().getNomDefunt(),
                                GraviteAudit.INFO);

                return restitutionMapper.toResponseDTO(saved);
        }

        @Override
        public RestitutionResponseDTO findById(Long id) {
                return restitutionRepository.findById(id)
                                .map(restitutionMapper::toResponseDTO)
                                .orElseThrow(() -> new ResourceNotFoundException("Restitution introuvable : " + id));
        }

        @Override
        public Page<RestitutionResponseDTO> findAll(StatutRestitution statut, Pageable pageable) {
                Page<Restitution> page = (statut != null)
                                ? restitutionRepository.findByStatut(statut, pageable)
                                : restitutionRepository.findAll(pageable);
                return page.map(restitutionMapper::toResponseDTO);
        }

        @Override
        public List<RestitutionResponseDTO> getPlanning(LocalDateTime date) {
                LocalDateTime debut = date.toLocalDate().atStartOfDay();
                LocalDateTime fin = date.toLocalDate().atTime(23, 59, 59);
                return restitutionRepository.findPlanning(debut, fin).stream()
                                .map(restitutionMapper::toResponseDTO)
                                .collect(Collectors.toList());
        }

        @Override
        public FactureResponseDTO getFactureByDepouille(Long depouilleId) {
                List<Facture> factures = factureRepository.findByDepouilleId(depouilleId);
                return factures.stream()
                                .filter(f -> f.getStatut() != StatutFacture.ANNULEE)
                                .max((f1, f2) -> f1.getId().compareTo(f2.getId()))
                                .map(factureMapper::toResponseDTO)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Aucune facture trouvée pour la dépouille : " + depouilleId));
        }

        @Override
        public byte[] genererAttestationRestitution(Long id) {
                Restitution r = restitutionRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Restitution introuvable : " + id));

                if (r.getStatut() != StatutRestitution.EFFECTUEE) {
                        throw new BusinessRuleException(
                                        "L'attestation ne peut être générée que pour une restitution EFFECTUEE.");
                }

                try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                        com.lowagie.text.Document doc = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4, 40,
                                        40, 60, 40);
                        com.lowagie.text.pdf.PdfWriter.getInstance(doc, baos);
                        doc.open();

                        // ── Couleurs & Polices ──────────────────────────────────────────
                        java.awt.Color bleuFonce = new java.awt.Color(30, 58, 138);
                        java.awt.Color bleuClaire = new java.awt.Color(219, 234, 254);
                        java.awt.Color gris = new java.awt.Color(107, 114, 128);
                        com.lowagie.text.Font fontTitre = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18,
                                        com.lowagie.text.Font.BOLD, bleuFonce);
                        com.lowagie.text.Font fontSousTitre = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA,
                                        10,
                                        com.lowagie.text.Font.NORMAL, gris);
                        com.lowagie.text.Font fontSection = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA,
                                        11,
                                        com.lowagie.text.Font.BOLD, bleuFonce);
                        com.lowagie.text.Font fontLabel = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9,
                                        com.lowagie.text.Font.BOLD, gris);
                        com.lowagie.text.Font fontValeur = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA,
                                        10,
                                        com.lowagie.text.Font.NORMAL, java.awt.Color.BLACK);
                        com.lowagie.text.Font fontSmall = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8,
                                        com.lowagie.text.Font.ITALIC, gris);

                        // ── En-tête ─────────────────────────────────────────────────────
                        com.lowagie.text.pdf.PdfPTable header = new com.lowagie.text.pdf.PdfPTable(2);
                        header.setWidthPercentage(100);
                        header.setWidths(new float[] { 70, 30 });

                        com.lowagie.text.pdf.PdfPCell cellLeft = new com.lowagie.text.pdf.PdfPCell();
                        cellLeft.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                        cellLeft.addElement(new com.lowagie.text.Paragraph("SYSTÈME DE GESTION DE MORGUE", fontTitre));
                        cellLeft.addElement(new com.lowagie.text.Paragraph("Attestation de Restitution de Corps",
                                        fontSousTitre));
                        header.addCell(cellLeft);

                        com.lowagie.text.pdf.PdfPCell cellRight = new com.lowagie.text.pdf.PdfPCell();
                        cellRight.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                        cellRight.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
                        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter
                                        .ofPattern("dd/MM/yyyy HH:mm");
                        cellRight.addElement(
                                        new com.lowagie.text.Paragraph(
                                                        "Émis le : " + java.time.LocalDateTime.now().format(fmt),
                                                        fontSmall));
                        header.addCell(cellRight);
                        doc.add(header);

                        // Ligne bleue
                        com.lowagie.text.pdf.PdfPTable sep = new com.lowagie.text.pdf.PdfPTable(1);
                        sep.setWidthPercentage(100);
                        sep.setSpacingBefore(5);
                        sep.setSpacingAfter(15);
                        com.lowagie.text.pdf.PdfPCell sepCell = new com.lowagie.text.pdf.PdfPCell(
                                        new com.lowagie.text.Phrase(" "));
                        sepCell.setBackgroundColor(bleuFonce);
                        sepCell.setFixedHeight(2);
                        sepCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                        sep.addCell(sepCell);
                        doc.add(sep);

                        // ── Corps de l'attestation ──────────────────────────────────────
                        com.lowagie.text.Paragraph intro = new com.lowagie.text.Paragraph(
                                        "Nous soussignés, Direction de la Morgue, certifions par la présente que la dépouille mortelle identifiée ci-dessous a été restituée à la famille conformément aux procédures en vigueur.",
                                        fontValeur);
                        intro.setSpacingAfter(20);
                        doc.add(intro);

                        // Section Défunt
                        doc.add(createPdfSectionTitle("IDENTIFICATION DU DÉFUNT", fontSection, bleuClaire));
                        com.lowagie.text.pdf.PdfPTable tabDefunt = new com.lowagie.text.pdf.PdfPTable(2);
                        tabDefunt.setWidthPercentage(100);
                        tabDefunt.setSpacingBefore(10);
                        addPdfInfoRow(tabDefunt, "Nom & Prénoms :",
                                        r.getDepouille().getNomDefunt() + " " + r.getDepouille().getPrenomDefunt(),
                                        fontLabel, fontValeur);
                        addPdfInfoRow(tabDefunt, "ID Unique :", r.getDepouille().getIdentifiantUnique(), fontLabel,
                                        fontValeur);
                        addPdfInfoRow(tabDefunt, "Date de décès :", r.getDepouille().getDateDeces().format(fmt),
                                        fontLabel,
                                        fontValeur);
                        doc.add(tabDefunt);

                        // Section Restitution
                        doc.add(createPdfSectionTitle("DÉTAILS DE LA RESTITUTION", fontSection, bleuClaire));
                        com.lowagie.text.pdf.PdfPTable tabRest = new com.lowagie.text.pdf.PdfPTable(2);
                        tabRest.setWidthPercentage(100);
                        tabRest.setSpacingBefore(10);
                        addPdfInfoRow(tabRest, "Date effective :", r.getDateEffective().format(fmt), fontLabel,
                                        fontValeur);
                        addPdfInfoRow(tabRest, "Bénéficiaire (Tuteur) :", r.getFamille().getTuteurLegal(), fontLabel,
                                        fontValeur);
                        addPdfInfoRow(tabRest, "Récupéré par :", r.getRepresentantFamille(), fontLabel, fontValeur);
                        addPdfInfoRow(tabRest, "Pièce d'Identité :", r.getPieceIdentiteRef(), fontLabel, fontValeur);
                        doc.add(tabRest);

                        // ── Signatures ──────────────────────────────────────────────────
                        com.lowagie.text.pdf.PdfPTable sigTable = new com.lowagie.text.pdf.PdfPTable(2);
                        sigTable.setWidthPercentage(100);
                        sigTable.setSpacingBefore(40);

                        com.lowagie.text.pdf.PdfPCell sigFamille = new com.lowagie.text.pdf.PdfPCell();
                        sigFamille.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                        sigFamille.addElement(
                                        new com.lowagie.text.Paragraph("Le Représentant de la Famille\n(Signature)",
                                                        fontLabel));
                        sigTable.addCell(sigFamille);

                        com.lowagie.text.pdf.PdfPCell sigMorgue = new com.lowagie.text.pdf.PdfPCell();
                        sigMorgue.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                        sigMorgue.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
                        com.lowagie.text.Paragraph pSig = new com.lowagie.text.Paragraph(
                                        "La Direction de la Morgue\n(Tampon et Signature)", fontLabel);
                        pSig.setAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
                        sigMorgue.addElement(pSig);
                        sigTable.addCell(sigMorgue);

                        doc.add(sigTable);

                        // Pied de page
                        com.lowagie.text.Paragraph footer = new com.lowagie.text.Paragraph(
                                        "\n\nDocument généré automatiquement. Toute rature ou surcharge annule la validité de cette attestation.",
                                        fontSmall);
                        footer.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                        doc.add(footer);

                        doc.close();
                        return baos.toByteArray();
                } catch (Exception e) {
                        throw new RuntimeException("Erreur lors de la génération de l'attestation de restitution", e);
                }
        }

        private com.lowagie.text.pdf.PdfPTable createPdfSectionTitle(String title, com.lowagie.text.Font font,
                        java.awt.Color bg) {
                com.lowagie.text.pdf.PdfPTable t = new com.lowagie.text.pdf.PdfPTable(1);
                t.setWidthPercentage(100);
                t.setSpacingBefore(15);
                com.lowagie.text.pdf.PdfPCell c = new com.lowagie.text.pdf.PdfPCell(
                                new com.lowagie.text.Phrase(title, font));
                c.setBackgroundColor(bg);
                c.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                c.setPadding(5);
                t.addCell(c);
                return t;
        }

        private void addPdfInfoRow(com.lowagie.text.pdf.PdfPTable table, String label, String value,
                        com.lowagie.text.Font fL, com.lowagie.text.Font fV) {
                com.lowagie.text.pdf.PdfPCell cL = new com.lowagie.text.pdf.PdfPCell(
                                new com.lowagie.text.Phrase(label, fL));
                cL.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                cL.setPadding(5);
                table.addCell(cL);

                com.lowagie.text.pdf.PdfPCell cV = new com.lowagie.text.pdf.PdfPCell(
                                new com.lowagie.text.Phrase(value != null ? value : "—", fV));
                cV.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                cV.setPadding(5);
                table.addCell(cV);
        }
}
