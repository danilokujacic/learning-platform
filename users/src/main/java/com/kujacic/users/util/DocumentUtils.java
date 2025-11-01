package com.kujacic.users.util;

import com.kujacic.users.model.Progress;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@UtilityClass
@Slf4j
public class DocumentUtils {
    public String formatDocumentName(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        LocalDate date = LocalDate.now();
        return username + "-" + date + "-" + "report";

    }

    public byte[] generateProgressDocument(List<Progress> progresses) throws IOException {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("User Progress");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Course", "Progress" };

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 1;
            for (Progress progress : progresses) {
                log.info("Progress loaded {}", progress.getId());
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(progress.getCourseName());
                row.createCell(1).setCellValue(progress.getProgress() + "%");
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
