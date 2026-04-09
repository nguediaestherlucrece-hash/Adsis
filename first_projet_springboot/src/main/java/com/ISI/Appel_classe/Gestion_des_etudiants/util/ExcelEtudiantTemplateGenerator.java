package com.ISI.Appel_classe.Gestion_des_etudiants.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Classe utilitaire pour générer un fichier Excel modèle pour l'import
 * d'étudiants
 * Ce fichier peut être téléchargé par l'utilisateur, rempli, et réuploadé
 */
@Slf4j
@Component
public class ExcelEtudiantTemplateGenerator {

    private static final String[] HEADERS = {
            "Nom",
            "Prénom",
            "Email",
            "Matricule",
            "Date Naissance (YYYY-MM-DD)",
            "Téléphone",
    };

    /**
     * Génère un fichier Excel modèle pour l'import d'étudiants
     *
     * Structure du fichier:
     * - Ligne 1: En-têtes
     * - Lignes 2-11: Exemples de données (optionnels, peuvent être supprimés)
     *
     * Colonnes:
     * - A: Nom (obligatoire)
     * - B: Prénom (obligatoire)
     * - C: Email (obligatoire)
     * - D: Matricule (obligatoire)
     * - E: Date Naissance (format YYYY-MM-DD, optionnel)
     * - F: Téléphone (optionnel)
     * 
     * @return Un tableau de bytes contenant le fichier Excel
     * @throws IOException En cas d'erreur lors de la génération
     */
    public byte[] generateTemplate() throws IOException {
        log.info("Génération du fichier Excel modèle pour l'import d'étudiants");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Étudiants");

            // Créer le style pour les en-têtes
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Créer la ligne d'en-têtes
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Définir la largeur des colonnes
            sheet.setColumnWidth(0, 15 * 256); // Nom
            sheet.setColumnWidth(1, 15 * 256); // Prénom
            sheet.setColumnWidth(2, 25 * 256); // Email
            sheet.setColumnWidth(3, 15 * 256); // Matricule
            sheet.setColumnWidth(4, 25 * 256); // Date Naissance
            sheet.setColumnWidth(5, 15 * 256); // Téléphone

            // Ajouter des lignes d'exemple (avec des données fictivielles)
            addExampleRows(sheet);

            // Geler la première ligne (en-têtes)
            sheet.createFreezePane(0, 1);

            // Convertir le workbook en bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            log.info("✅ Fichier Excel modèle généré avec succès");
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("❌ Erreur lors de la génération du fichier Excel modèle", e);
            throw e;
        }
    }

    /**
     * Crée le style pour les en-têtes (fond bleu, texte blanc, gras)
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // Fond bleu
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Police blanche et gras
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        // Alignement centré
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // Bordures
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * Ajoute des lignes d'exemple au fichier modèle
     * Ces lignes peuvent être supprimées ou remplacées par l'utilisateur
     */
    private void addExampleRows(Sheet sheet) {
        String[][] exampleData = {
                { "Eboa", "Samuel", "samuel.eboa@saintjeaningenieur.org", "MAT001", "2005-01-15", "+237670123456" },
                { "Ngo Njock", "Thérèse", "therese.ngonjock@saintjeaningenieur.org", "MAT002", "2004-05-22",
                        "+237690654321" },
                { "Kamdem", "Jean-Paul", "jp.kamdem@saintjeaningenieur.org", "MAT003", "2005-08-30", "+237651987654" },
                { "Abena", "Fouda Marie", "marie.abena@saintjeaningenieur.org", "MAT004", "2006-03-10",
                        "+237620112233" },
                { "Moussa", "Ibrahim", "ibrahim.moussa@saintjeaningenieur.org", "MAT005", "2005-11-25",
                        "+237681887766" }
        };

        CellStyle exampleStyle = sheet.getWorkbook().createCellStyle();
        exampleStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        exampleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        exampleStyle.setBorderBottom(BorderStyle.THIN);
        exampleStyle.setBorderTop(BorderStyle.THIN);
        exampleStyle.setBorderLeft(BorderStyle.THIN);
        exampleStyle.setBorderRight(BorderStyle.THIN);

        for (int i = 0; i < exampleData.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < exampleData[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(exampleData[i][j]);
                cell.setCellStyle(exampleStyle);
            }
        }

        log.debug("5 lignes d'exemple ajoutées au fichier modèle");
    }
}
