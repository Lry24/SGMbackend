package com.sgm.SGMbackend.serviceImpl;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.Famille;
import com.sgm.SGMbackend.service.BonReceptionService;
import com.sgm.SGMbackend.service.DepouilleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implémentation du service de génération du Bon de Réception en PDF (OpenPDF).
 */
@Service
@RequiredArgsConstructor
public class BonReceptionServiceImpl implements BonReceptionService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DepouilleService depouilleService;

    @Override
    public byte[] genererBonReception(Long depouilleId) {
        Depouille d = depouilleService.findById(depouilleId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 60, 40);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // ── Couleurs ─────────────────────────────────────────────────────
            Color bleuFonce = new Color(30, 58, 138);
            Color bleuClaire = new Color(219, 234, 254);
            Color gris = new Color(107, 114, 128);
            Color noir = Color.BLACK;

            // ── Polices ──────────────────────────────────────────────────────
            Font fontTitre = new Font(Font.HELVETICA, 18, Font.BOLD, bleuFonce);
            Font fontSousTitre = new Font(Font.HELVETICA, 10, Font.NORMAL, gris);
            Font fontSection = new Font(Font.HELVETICA, 11, Font.BOLD, bleuFonce);
            Font fontLabel = new Font(Font.HELVETICA, 9, Font.BOLD, gris);
            Font fontValeur = new Font(Font.HELVETICA, 10, Font.NORMAL, noir);
            Font fontSmall = new Font(Font.HELVETICA, 8, Font.ITALIC, gris);
            Font fontId = new Font(Font.HELVETICA, 14, Font.BOLD, bleuFonce);

            // ── En-tête ───────────────────────────────────────────────────────
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[] { 60, 40 });

            PdfPCell cellLeft = new PdfPCell();
            cellLeft.setBorder(Rectangle.NO_BORDER);
            cellLeft.setPaddingBottom(10);
            cellLeft.addElement(new Paragraph("SYSTÈME DE GESTION DE MORGUE", fontTitre));
            cellLeft.addElement(new Paragraph("Bon de Réception — Document officiel", fontSousTitre));
            header.addCell(cellLeft);

            PdfPCell cellRight = new PdfPCell();
            cellRight.setBorder(Rectangle.NO_BORDER);
            cellRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellRight.setPaddingBottom(10);
            cellRight.addElement(new Paragraph(safe(d.getIdentifiantUnique()), fontId));
            cellRight.addElement(new Paragraph("Émis le : " + LocalDateTime.now().format(FMT), fontSmall));
            header.addCell(cellRight);

            doc.add(header);

            // Ligne séparatrice bleue
            PdfPTable sep = new PdfPTable(1);
            sep.setWidthPercentage(100);
            sep.setSpacingBefore(2);
            sep.setSpacingAfter(14);
            PdfPCell sepCell = new PdfPCell(new Phrase(" "));
            sepCell.setBackgroundColor(bleuFonce);
            sepCell.setFixedHeight(2);
            sepCell.setBorder(Rectangle.NO_BORDER);
            sep.addCell(sepCell);
            doc.add(sep);

            // ── Section Défunt ────────────────────────────────────────────────
            doc.add(sectionTitle("Informations sur le Défunt", fontSection, bleuClaire));

            PdfPTable infoTable = new PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(6);
            infoTable.setSpacingAfter(14);

            addRow(infoTable,
                    "Nom", safe(d.getNomDefunt()),
                    "Prénom", safe(d.getPrenomDefunt()),
                    fontLabel, fontValeur);
            addRow(infoTable,
                    "Date de naissance", d.getDateNaissance() != null ? d.getDateNaissance().format(FMT_DATE) : "—",
                    "Date de décès", d.getDateDeces() != null ? d.getDateDeces().format(FMT) : "—",
                    fontLabel, fontValeur);
            addRow(infoTable,
                    "Cause présumée", safe(d.getCausePresumee()),
                    "Provenance", safe(d.getProvenance()),
                    fontLabel, fontValeur);

            doc.add(infoTable);

            // ── Section Réception ─────────────────────────────────────────────
            doc.add(sectionTitle("Réception à la Morgue", fontSection, bleuClaire));

            PdfPTable recepTable = new PdfPTable(4);
            recepTable.setWidthPercentage(100);
            recepTable.setSpacingBefore(6);
            recepTable.setSpacingAfter(14);

            addRow(recepTable,
                    "Date d'arrivée", d.getDateArrivee() != null ? d.getDateArrivee().format(FMT) : "—",
                    "Statut actuel", d.getStatut() != null ? d.getStatut().name().replace("_", " ") : "—",
                    fontLabel, fontValeur);

            String empl = "—";
            if (d.getEmplacement() != null && d.getEmplacement().getCode() != null) {
                empl = d.getEmplacement().getCode();
            }

            addRow(recepTable,
                    "Emplacement", empl,
                    "Observations", safe(d.getObservations()),
                    fontLabel, fontValeur);

            doc.add(recepTable);

            // ── Section Famille ───────────────────────────────────────────────
            Famille famille = d.getFamille();
            if (famille != null) {
                doc.add(sectionTitle("Référent Familial", fontSection, bleuClaire));

                PdfPTable familleTable = new PdfPTable(4);
                familleTable.setWidthPercentage(100);
                familleTable.setSpacingBefore(6);
                familleTable.setSpacingAfter(14);

                addRow(familleTable,
                        "Tuteur légal", safe(famille.getTuteurLegal()),
                        "Téléphone", safe(famille.getTelephone()),
                        fontLabel, fontValeur);
                addRow(familleTable,
                        "Lien avec le défunt", safe(famille.getLienParente()),
                        "Email", safe(famille.getEmail()),
                        fontLabel, fontValeur);

                doc.add(familleTable);
            }

            // ── Zones de signature ────────────────────────────────────────────
            PdfPTable sigTable = new PdfPTable(3);
            sigTable.setWidthPercentage(100);
            sigTable.setSpacingBefore(30);

            for (String sigLabel : new String[] { "Agent Réceptionnaire", "Responsable Morgue",
                    "Représentant Famille" }) {
                PdfPCell cell = new PdfPCell();
                cell.setBorder(Rectangle.BOX);
                cell.setBorderColor(new Color(209, 213, 219));
                cell.setPadding(12);
                Paragraph p = new Paragraph(sigLabel + "\n\n\n\n\nSignature :", fontLabel);
                p.setAlignment(Element.ALIGN_CENTER);
                cell.addElement(p);
                sigTable.addCell(cell);
            }
            doc.add(sigTable);

            // ── Pied de page ──────────────────────────────────────────────────
            Paragraph footer = new Paragraph(
                    "Ce bon de réception est un document officiel. " +
                            "Toute modification est invalide sans tampon et signature de l'établissement.",
                    fontSmall);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(16);
            doc.add(footer);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du bon de réception PDF", e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String safe(String val) {
        return val != null ? val : "—";
    }

    private PdfPTable sectionTitle(String title, Font font, Color bgColor) throws DocumentException {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(6);
        PdfPCell cell = new PdfPCell(new Phrase(title, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setBorder(Rectangle.NO_BORDER);
        t.addCell(cell);
        return t;
    }

    private void addRow(PdfPTable table,
            String label1, String val1,
            String label2, String val2,
            Font fontLabel, Font fontVal) {
        table.addCell(labelCell(label1, fontLabel));
        table.addCell(valueCell(val1, fontVal));
        table.addCell(labelCell(label2, fontLabel));
        table.addCell(valueCell(val2, fontVal));
    }

    private PdfPCell labelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(safe(text), font));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(new Color(229, 231, 235));
        cell.setPadding(6);
        cell.setBackgroundColor(new Color(249, 250, 251));
        return cell;
    }

    private PdfPCell valueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(safe(text), font));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(new Color(229, 231, 235));
        cell.setPadding(6);
        return cell;
    }
}
