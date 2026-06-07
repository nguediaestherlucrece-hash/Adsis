package com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.util;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.CoursUE;
import com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.dto.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExcelExportUtils {

    private static XSSFCellStyle createHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Segoe UI");
        style.setFont(font);

        // #1a365d (Deep Slate Blue)
        byte[] rgb = new byte[]{(byte) 26, (byte) 54, (byte) 93};
        style.setFillForegroundColor(new XSSFColor(rgb, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private static CellStyle createTitleStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        font.setFontName("Segoe UI");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private static CellStyle createDataStyle(XSSFWorkbook workbook, boolean bold) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Segoe UI");
        font.setFontHeightInPoints((short) 10);
        font.setBold(bold);
        style.setFont(font);
        setBorders(style);
        return style;
    }

    private static CellStyle createCenterDataStyle(XSSFWorkbook workbook, boolean bold) {
        CellStyle style = createDataStyle(workbook, bold);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
    }

    public static ByteArrayInputStream exportStudentHistory(List<PresenceHistoryDTO> history, StudentPresenceStatsDTO stats) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Historique Présences");
            sheet.setDisplayGridlines(true);

            // Title Block
            Row titleRow = sheet.createRow(1);
            Cell titleCell = titleRow.createCell(1);
            titleCell.setCellValue("HISTORIQUE PERSONNEL DE PRÉSENCE");
            titleCell.setCellStyle(createTitleStyle(workbook));

            // Stats Block
            CellStyle labelStyle = createDataStyle(workbook, true);
            CellStyle valStyle = createDataStyle(workbook, false);

            Row r3 = sheet.createRow(3);
            r3.createCell(1).setCellValue("Taux de présence global :");
            r3.getCell(1).setCellStyle(labelStyle);
            r3.createCell(2).setCellValue(String.format("%.2f %%", stats.getTauxPresenceGlobal()));
            r3.getCell(2).setCellStyle(valStyle);

            Row r4 = sheet.createRow(4);
            r4.createCell(1).setCellValue("Absences Justifiées :");
            r4.getCell(1).setCellStyle(labelStyle);
            r4.createCell(2).setCellValue(stats.getTotalHeuresAbsentesJustifiees() + " h (" + stats.getTotalSeancesAbsentesJustifiees() + " séances)");
            r4.getCell(2).setCellStyle(valStyle);

            Row r5 = sheet.createRow(5);
            r5.createCell(1).setCellValue("Absences Non-Justifiées :");
            r5.getCell(1).setCellStyle(labelStyle);
            r5.createCell(2).setCellValue(stats.getTotalHeuresAbsentesNonJustifiees() + " h (" + stats.getTotalSeancesAbsentesNonJustifiees() + " séances)");
            r5.getCell(2).setCellStyle(valStyle);

            Row r6 = sheet.createRow(6);
            r6.createCell(1).setCellValue("Nombre de retards :");
            r6.getCell(1).setCellStyle(labelStyle);
            r6.createCell(2).setCellValue(stats.getTotalRetards() + " retard(s)");
            r6.getCell(2).setCellStyle(valStyle);

            // Table Header
            Row headerRow = sheet.createRow(9);
            headerRow.setHeightInPoints(28);
            String[] headers = {"Date", "Matière (Code)", "Intitulé", "Horaire", "Statut", "Présent (h)", "Absent (h)", "Justifié", "Statut Justif."};
            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i + 1);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data Rows
            CellStyle dataStyle = createDataStyle(workbook, false);
            CellStyle centerStyle = createCenterDataStyle(workbook, false);
            int rowIdx = 10;
            for (PresenceHistoryDTO item : history) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(1).setCellValue(item.getDate() != null ? item.getDate().toString() : "—");
                row.createCell(2).setCellValue(item.getMatiereCode());
                row.createCell(3).setCellValue(item.getMatiereIntitule());
                row.createCell(4).setCellValue(item.getHeureDebut() + " - " + item.getHeureFin());
                row.createCell(5).setCellValue(item.getStatut());
                row.createCell(6).setCellValue(item.getHeuresPresentes());
                row.createCell(7).setCellValue(item.getHeuresAbsentes());
                row.createCell(8).setCellValue(item.isJustifiee() ? "Oui" : "Non");
                row.createCell(9).setCellValue(item.getJustificatifStatut() != null ? item.getJustificatifStatut() : "—");

                for (int col = 1; col <= 9; col++) {
                    row.getCell(col).setCellStyle(col == 2 || col == 3 ? dataStyle : centerStyle);
                }
            }

            for (int i = 1; i <= 9; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Erreur de génération du fichier Excel", e);
        }
    }

    public static ByteArrayInputStream exportTeacherHistory(List<TeacherSeanceStatsDTO> stats) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Historique Appels");
            sheet.setDisplayGridlines(true);

            Row titleRow = sheet.createRow(1);
            Cell titleCell = titleRow.createCell(1);
            titleCell.setCellValue("HISTORIQUE DES APPELS - ENSEIGNANT");
            titleCell.setCellStyle(createTitleStyle(workbook));

            Row headerRow = sheet.createRow(4);
            headerRow.setHeightInPoints(28);
            String[] headers = {"Date", "Classe", "Matière (Code)", "Intitulé", "Horaire", "Total Étudiants", "Présents", "Absents Justifiés", "Absents Injustifiés", "Retards", "Taux Présence"};
            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i + 1);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            CellStyle dataStyle = createDataStyle(workbook, false);
            CellStyle centerStyle = createCenterDataStyle(workbook, false);
            int rowIdx = 5;
            for (TeacherSeanceStatsDTO item : stats) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(1).setCellValue(item.getDate() != null ? item.getDate().toString() : "—");
                row.createCell(2).setCellValue(item.getNomClasse());
                row.createCell(3).setCellValue(item.getCodeUE());
                row.createCell(4).setCellValue(item.getIntitule());
                row.createCell(5).setCellValue(item.getHeureDebut() + " - " + item.getHeureFin());
                row.createCell(6).setCellValue(item.getTotalEtudiants());
                row.createCell(7).setCellValue(item.getPresents());
                row.createCell(8).setCellValue(item.getAbsentsJustifies());
                row.createCell(9).setCellValue(item.getAbsentsNonJustifies());
                row.createCell(10).setCellValue(item.getRetards());
                row.createCell(11).setCellValue(String.format("%.2f %%", item.getTauxPresence()));

                for (int col = 1; col <= 11; col++) {
                    row.getCell(col).setCellStyle(col == 3 || col == 4 ? dataStyle : centerStyle);
                }
            }

            for (int i = 1; i <= 11; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Erreur de génération du fichier Excel", e);
        }
    }

    public static ByteArrayInputStream exportClassAbsenceReport(
            List<ClassAbsenceReportDTO> report, List<CoursUE> subjects, String className, String periodName) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Absences Classe");
            sheet.setDisplayGridlines(true);

            Row titleRow = sheet.createRow(1);
            Cell titleCell = titleRow.createCell(1);
            titleCell.setCellValue("RAPPORT DES ABSENCES : CLASSE " + className + " (" + periodName + ")");
            titleCell.setCellStyle(createTitleStyle(workbook));

            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook, false);
            CellStyle centerStyle = createCenterDataStyle(workbook, false);

            // Multilevel Headers
            Row rowSubjHeader = sheet.createRow(4);
            rowSubjHeader.setHeightInPoints(24);
            Row rowSubColHeader = sheet.createRow(5);
            rowSubColHeader.setHeightInPoints(20);

            // First 3 Columns merged vertically
            String[] studentHeaders = {"Matricule", "Nom", "Prénom"};
            for (int i = 0; i < studentHeaders.length; i++) {
                sheet.addMergedRegion(new CellRangeAddress(4, 5, i + 1, i + 1));
                Cell c = rowSubjHeader.createCell(i + 1);
                c.setCellValue(studentHeaders[i]);
                c.setCellStyle(headerStyle);

                // POI requires empty cells in merged areas to apply styles correctly
                Cell cEmpty = rowSubColHeader.createCell(i + 1);
                cEmpty.setCellStyle(headerStyle);
            }

            // Subject columns
            int colIdx = 4;
            for (CoursUE s : subjects) {
                // Merge subject name over 2 sub-columns (Absence hours & Justified hours)
                sheet.addMergedRegion(new CellRangeAddress(4, 4, colIdx, colIdx + 1));
                Cell cSubj = rowSubjHeader.createCell(colIdx);
                cSubj.setCellValue(s.getIntitule() != null ? s.getIntitule() : s.getCodeUE());
                cSubj.setCellStyle(headerStyle);
                rowSubjHeader.createCell(colIdx + 1).setCellStyle(headerStyle);

                // Sub-columns
                Cell cAbs = rowSubColHeader.createCell(colIdx);
                cAbs.setCellValue("Abs (h)");
                cAbs.setCellStyle(headerStyle);

                Cell cJust = rowSubColHeader.createCell(colIdx + 1);
                cJust.setCellValue("Just (h)");
                cJust.setCellStyle(headerStyle);

                colIdx += 2;
            }

            // Data rows
            int rowIdx = 6;
            for (ClassAbsenceReportDTO rowData : report) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(1).setCellValue(rowData.getMatricule());
                row.createCell(2).setCellValue(rowData.getNom());
                row.createCell(3).setCellValue(rowData.getPrenom());
                row.getCell(1).setCellStyle(centerStyle);
                row.getCell(2).setCellStyle(dataStyle);
                row.getCell(3).setCellStyle(dataStyle);

                int currentCol = 4;
                for (CoursUE s : subjects) {
                    Double abs = rowData.getHeuresAbsencesParCours().get(s.getId());
                    Double just = rowData.getHeuresJustifieesParCours().get(s.getId());

                    Cell cellAbs = row.createCell(currentCol);
                    cellAbs.setCellValue(abs != null ? abs : 0.0);
                    cellAbs.setCellStyle(centerStyle);

                    Cell cellJust = row.createCell(currentCol + 1);
                    cellJust.setCellValue(just != null ? just : 0.0);
                    cellJust.setCellStyle(centerStyle);

                    currentCol += 2;
                }
            }

            for (int i = 1; i < colIdx; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Erreur de génération Excel", e);
        }
    }

    public static ByteArrayInputStream exportClassMinusReport(
            List<ClassMinusReportDTO> report, List<CoursUE> subjects, String className) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Pénalités CC Classe");
            sheet.setDisplayGridlines(true);

            Row titleRow = sheet.createRow(1);
            Cell titleCell = titleRow.createCell(1);
            titleCell.setCellValue("SUIVI DES PÉNALITÉS SUR NOTE DE CC : CLASSE " + className);
            titleCell.setCellStyle(createTitleStyle(workbook));

            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook, false);
            CellStyle centerStyle = createCenterDataStyle(workbook, false);

            Row rowSubjHeader = sheet.createRow(4);
            rowSubjHeader.setHeightInPoints(24);
            Row rowSubColHeader = sheet.createRow(5);
            rowSubColHeader.setHeightInPoints(20);

            String[] studentHeaders = {"Matricule", "Nom", "Prénom"};
            for (int i = 0; i < studentHeaders.length; i++) {
                sheet.addMergedRegion(new CellRangeAddress(4, 5, i + 1, i + 1));
                Cell c = rowSubjHeader.createCell(i + 1);
                c.setCellValue(studentHeaders[i]);
                c.setCellStyle(headerStyle);
                rowSubColHeader.createCell(i + 1).setCellStyle(headerStyle);
            }

            // Subject columns
            int colIdx = 4;
            for (CoursUE s : subjects) {
                // Merge subject name over 3 columns: Completed hours, Unjustified hours, Malus
                sheet.addMergedRegion(new CellRangeAddress(4, 4, colIdx, colIdx + 2));
                Cell cSubj = rowSubjHeader.createCell(colIdx);
                cSubj.setCellValue(s.getIntitule() != null ? s.getIntitule() : s.getCodeUE());
                cSubj.setCellStyle(headerStyle);
                rowSubjHeader.createCell(colIdx + 1).setCellStyle(headerStyle);
                rowSubjHeader.createCell(colIdx + 2).setCellStyle(headerStyle);

                Cell cEff = rowSubColHeader.createCell(colIdx);
                cEff.setCellValue("Eff. (h)");
                cEff.setCellStyle(headerStyle);

                Cell cUnj = rowSubColHeader.createCell(colIdx + 1);
                cUnj.setCellValue("Injust. (h)");
                cUnj.setCellStyle(headerStyle);

                Cell cPen = rowSubColHeader.createCell(colIdx + 2);
                cPen.setCellValue("Malus CC");
                cPen.setCellStyle(headerStyle);

                colIdx += 3;
            }

            // Data rows
            int rowIdx = 6;
            for (ClassMinusReportDTO rowData : report) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(1).setCellValue(rowData.getMatricule());
                row.createCell(2).setCellValue(rowData.getNom());
                row.createCell(3).setCellValue(rowData.getPrenom());
                row.getCell(1).setCellStyle(centerStyle);
                row.getCell(2).setCellStyle(dataStyle);
                row.getCell(3).setCellStyle(dataStyle);

                int currentCol = 4;
                for (CoursUE s : subjects) {
                    Double eff = rowData.getHeuresEffectueesParCours().get(s.getId());
                    Double unj = rowData.getHeuresInjustifieesParCours().get(s.getId());
                    Boolean malus = rowData.getAMalusParCours().get(s.getId());

                    row.createCell(currentCol).setCellValue(eff != null ? eff : 0.0);
                    row.getCell(currentCol).setCellStyle(centerStyle);

                    row.createCell(currentCol + 1).setCellValue(unj != null ? unj : 0.0);
                    row.getCell(currentCol + 1).setCellStyle(centerStyle);

                    Cell cellMalus = row.createCell(currentCol + 2);
                    cellMalus.setCellValue(malus != null && malus ? "-1 pt" : "Aucun");
                    cellMalus.setCellStyle(centerStyle);

                    currentCol += 3;
                }
            }

            for (int i = 1; i < colIdx; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Erreur de génération Excel", e);
        }
    }

    public static ByteArrayInputStream exportClassSNAdmissionReport(
            List<ClassSNAdmissionReportDTO> report, List<CoursUE> subjects, String className) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Admissions SN");
            sheet.setDisplayGridlines(true);

            Row titleRow = sheet.createRow(1);
            Cell titleCell = titleRow.createCell(1);
            titleCell.setCellValue("ADMISSION À LA SESSION NORMALE : CLASSE " + className);
            titleCell.setCellStyle(createTitleStyle(workbook));

            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook, false);
            CellStyle centerStyle = createCenterDataStyle(workbook, false);

            Row rowHeader = sheet.createRow(4);
            rowHeader.setHeightInPoints(28);

            rowHeader.createCell(1).setCellValue("Matricule");
            rowHeader.getCell(1).setCellStyle(headerStyle);
            rowHeader.createCell(2).setCellValue("Nom");
            rowHeader.getCell(2).setCellStyle(headerStyle);
            rowHeader.createCell(3).setCellValue("Prénom");
            rowHeader.getCell(3).setCellStyle(headerStyle);

            int colIdx = 4;
            for (CoursUE s : subjects) {
                Cell cellSubj = rowHeader.createCell(colIdx++);
                cellSubj.setCellValue(s.getIntitule() != null ? s.getIntitule() : s.getCodeUE());
                cellSubj.setCellStyle(headerStyle);
            }

            int rowIdx = 5;
            for (ClassSNAdmissionReportDTO rowData : report) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(1).setCellValue(rowData.getMatricule());
                row.createCell(2).setCellValue(rowData.getNom());
                row.createCell(3).setCellValue(rowData.getPrenom());
                row.getCell(1).setCellStyle(centerStyle);
                row.getCell(2).setCellStyle(dataStyle);
                row.getCell(3).setCellStyle(dataStyle);

                int currentCol = 4;
                for (CoursUE s : subjects) {
                    String snStatus = rowData.getAdmissionSNParCours().get(s.getId());
                    Cell cellSN = row.createCell(currentCol++);
                    cellSN.setCellValue(snStatus != null ? snStatus : "sn oui");
                    cellSN.setCellStyle(centerStyle);
                }
            }

            for (int i = 1; i < colIdx; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Erreur de génération Excel", e);
        }
    }
}
