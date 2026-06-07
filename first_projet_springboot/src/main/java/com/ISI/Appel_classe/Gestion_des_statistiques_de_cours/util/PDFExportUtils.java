package com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.util;

import com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.dto.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class PDFExportUtils {

    private static final Color PRIMARY_COLOR = new Color(26, 54, 93);   // #1a365d (Deep Slate Blue)
    private static final Color SECONDARY_COLOR = new Color(43, 108, 176); // #2b6cb0
    private static final Color LIGHT_GREY = new Color(247, 250, 252);     // #f7fafc
    private static final Color TEXT_COLOR = new Color(45, 55, 72);       // #2d3748

    public static ByteArrayInputStream exportStudentHistoryPdf(
            List<PresenceHistoryDTO> history,
            StudentPresenceStatsDTO stats,
            String studentName,
            String studentMatricule,
            String className
    ) {
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, PRIMARY_COLOR);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, SECONDARY_COLOR);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, TEXT_COLOR);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_COLOR);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

            // Document Header
            Paragraph title = new Paragraph("HISTORIQUE PERSONNEL DE PRÉSENCE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            // Student Information
            Paragraph info = new Paragraph();
            info.add(new Chunk("Étudiant : ", boldFont));
            info.add(new Chunk(studentName + "\n", regularFont));
            info.add(new Chunk("Matricule : ", boldFont));
            info.add(new Chunk(studentMatricule + "\n", regularFont));
            info.add(new Chunk("Classe : ", boldFont));
            info.add(new Chunk(className != null ? className : "—", regularFont));
            info.setSpacingAfter(15);
            document.add(info);

            // Stats Summary Box
            PdfPTable statsTable = new PdfPTable(4);
            statsTable.setWidthPercentage(100);
            statsTable.setSpacingAfter(20);

            addStatsCell(statsTable, "Taux de Présence Global", String.format("%.2f %%", stats.getTauxPresenceGlobal()), boldFont, regularFont);
            addStatsCell(statsTable, "Absences Justifiées", stats.getTotalHeuresAbsentesJustifiees() + " h (" + stats.getTotalSeancesAbsentesJustifiees() + " sé.)", boldFont, regularFont);
            addStatsCell(statsTable, "Absences Non-Justifiées", stats.getTotalHeuresAbsentesNonJustifiees() + " h (" + stats.getTotalSeancesAbsentesNonJustifiees() + " sé.)", boldFont, regularFont);
            addStatsCell(statsTable, "Nombre de Retards", stats.getTotalRetards() + " retards", boldFont, regularFont);
            document.add(statsTable);

            // Detailed Attendance Table
            Paragraph subtitle = new Paragraph("DÉTAIL DE LA PÉRIODE", sectionFont);
            subtitle.setSpacingAfter(10);
            document.add(subtitle);

            String[] headers = {"Date", "Code UE", "Intitulé Matière", "Horaire", "Statut", "Présent (h)", "Absent (h)", "Justifié", "Validation"};
            float[] widths = {2.5f, 2.5f, 5f, 3.5f, 3f, 2.5f, 2.5f, 2f, 2.5f};

            PdfPTable table = new PdfPTable(widths);
            table.setWidthPercentage(100);

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(PRIMARY_COLOR);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(6);
                table.addCell(cell);
            }

            boolean alternate = false;
            for (PresenceHistoryDTO item : history) {
                Color bgColor = alternate ? LIGHT_GREY : Color.WHITE;
                alternate = !alternate;

                table.addCell(createCell(item.getDate() != null ? item.getDate().toString() : "—", regularFont, Element.ALIGN_CENTER, bgColor));
                table.addCell(createCell(item.getMatiereCode(), regularFont, Element.ALIGN_LEFT, bgColor));
                table.addCell(createCell(item.getMatiereIntitule(), regularFont, Element.ALIGN_LEFT, bgColor));
                table.addCell(createCell(item.getHeureDebut() + " - " + item.getHeureFin(), regularFont, Element.ALIGN_CENTER, bgColor));
                table.addCell(createCell(item.getStatut(), regularFont, Element.ALIGN_CENTER, bgColor));
                table.addCell(createCell(String.valueOf(item.getHeuresPresentes()), regularFont, Element.ALIGN_CENTER, bgColor));
                table.addCell(createCell(String.valueOf(item.getHeuresAbsentes()), regularFont, Element.ALIGN_CENTER, bgColor));
                table.addCell(createCell(item.isJustifiee() ? "Oui" : "Non", regularFont, Element.ALIGN_CENTER, bgColor));
                table.addCell(createCell(item.getJustificatifStatut() != null ? item.getJustificatifStatut() : "—", regularFont, Element.ALIGN_CENTER, bgColor));
            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    public static ByteArrayInputStream exportTeacherHistoryPdf(
            List<TeacherSeanceStatsDTO> stats,
            String teacherName
    ) {
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, PRIMARY_COLOR);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, TEXT_COLOR);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_COLOR);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

            Paragraph title = new Paragraph("RAPPORT DES APPELS DE COURS", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            Paragraph info = new Paragraph();
            info.add(new Chunk("Enseignant : ", boldFont));
            info.add(new Chunk(teacherName + "\n", regularFont));
            info.setSpacingAfter(15);
            document.add(info);

            String[] headers = {"Date", "Classe", "Code UE", "Intitulé Matière", "Horaire", "Étudiants", "Presents", "Abs. Justifiés", "Abs. Injustifiés", "Retards", "Taux Présence"};
            float[] widths = {2.5f, 2.5f, 2.5f, 5f, 3.5f, 2f, 2f, 2f, 2f, 2f, 3f};

            PdfPTable table = new PdfPTable(widths);
            table.setWidthPercentage(100);

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(PRIMARY_COLOR);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(6);
                table.addCell(cell);
            }

            boolean alternate = false;
            for (TeacherSeanceStatsDTO item : stats) {
                Color bgColor = alternate ? LIGHT_GREY : Color.WHITE;
                alternate = !alternate;

                table.addCell(createCell(item.getDate() != null ? item.getDate().toString() : "—", regularFont, Element.ALIGN_CENTER, bgColor));
                table.addCell(createCell(item.getNomClasse(), regularFont, Element.ALIGN_LEFT, bgColor));
                table.addCell(createCell(item.getCodeUE(), regularFont, Element.ALIGN_LEFT, bgColor));
                table.addCell(createCell(item.getIntitule(), regularFont, Element.ALIGN_LEFT, bgColor));
                table.addCell(createCell(item.getHeureDebut() + " - " + item.getHeureFin(), regularFont, Element.ALIGN_CENTER, bgColor));
                table.addCell(createCell(String.valueOf(item.getTotalEtudiants()), regularFont, Element.ALIGN_CENTER, bgColor));
                table.addCell(createCell(String.valueOf(item.getPresents()), regularFont, Element.ALIGN_CENTER, bgColor));
                table.addCell(createCell(String.valueOf(item.getAbsentsJustifies()), regularFont, Element.ALIGN_CENTER, bgColor));
                table.addCell(createCell(String.valueOf(item.getAbsentsNonJustifies()), regularFont, Element.ALIGN_CENTER, bgColor));
                table.addCell(createCell(String.valueOf(item.getRetards()), regularFont, Element.ALIGN_CENTER, bgColor));
                table.addCell(createCell(String.format("%.2f %%", item.getTauxPresence()), regularFont, Element.ALIGN_CENTER, bgColor));
            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Erreur de génération PDF", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private static void addStatsCell(PdfPTable table, String label, String value, Font boldFont, Font regularFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(LIGHT_GREY);
        cell.setPadding(8);
        cell.setBorderColor(new Color(226, 232, 240));

        Paragraph pLabel = new Paragraph(label.toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, SECONDARY_COLOR));
        pLabel.setSpacingAfter(4);
        cell.addElement(pLabel);

        Paragraph pValue = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY_COLOR));
        cell.addElement(pValue);

        table.addCell(cell);
    }

    private static PdfPCell createCell(String text, Font font, int alignment, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setBackgroundColor(bgColor);
        cell.setBorderColor(new Color(226, 232, 240));
        return cell;
    }
}
