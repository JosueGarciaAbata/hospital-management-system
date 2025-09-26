package com.drtx.jdit.reportservice.utils;

import com.drtx.jdit.reportservice.dto.ReportResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Utility for exporting reports to different formats
 */
@Component
public class ReportExportUtil {

    private final ObjectMapper objectMapper;
    
    public ReportExportUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Exports a report to Excel format
     * @param <T> data type of the report
     * @param report the report to export
     * @param sheetName name of the sheet
     * @return byte array with Excel content
     */
    public <T> byte[] exportToExcel(ReportResponseDTO<T> report, String sheetName) {
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet(sheetName);
            
            // Header styles
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // Create headers
            List<T> data = report.getData();
            if (data == null || data.isEmpty()) {
                Row row = sheet.createRow(0);
                Cell cell = row.createCell(0);
                cell.setCellValue("No data available");
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
            
            // Extract column names from first entity
            T firstEntity = data.get(0);
            Field[] fields = firstEntity.getClass().getDeclaredFields();
            
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < fields.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(formatFieldName(fields[i].getName()));
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }
            
            // Fill with data
            int rowNum = 1;
            for (T entity : data) {
                Row row = sheet.createRow(rowNum++);
                
                for (int i = 0; i < fields.length; i++) {
                    Cell cell = row.createCell(i);
                    fields[i].setAccessible(true);
                    
                    try {
                        Object value = fields[i].get(entity);
                        setCellValueBasedOnType(cell, value);
                    } catch (Exception e) {
                        // log.error("Error al acceder al campo: " + fields[i].getName(), e);
                        cell.setCellValue("");
                    }
                }
            }
            
            // Adjust column widths
            for (int i = 0; i < fields.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Add summary sheet if additional data exists
            if (report.getAdditionalData() != null) {
                Sheet summarySheet = workbook.createSheet("Summary");
                createSummarySheet(summarySheet, report.getAdditionalData(), headerStyle);
            }
            
            // Add metadata sheet
            Sheet metadataSheet = workbook.createSheet("Metadata");
            createMetadataSheet(metadataSheet, report.getMetadata(), headerStyle);
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            // log.error("Error al exportar a Excel", e);
            throw new RuntimeException("Error al generar el archivo Excel", e);
        }
    }
    
    /**
     * Exports a report to CSV format
     * @param <T> data type of the report
     * @param report the report to export
     * @return string with CSV content
     */
    public <T> String exportToCsv(ReportResponseDTO<T> report) {
        StringBuilder csv = new StringBuilder();
        List<T> data = report.getData();
        
        if (data == null || data.isEmpty()) {
            return "No data available";
        }
        
        // Extract column names from the first entity
        T firstEntity = data.get(0);
        Field[] fields = firstEntity.getClass().getDeclaredFields();
        
        // Headers
        for (int i = 0; i < fields.length; i++) {
            csv.append("\"").append(formatFieldName(fields[i].getName())).append("\"");
            if (i < fields.length - 1) {
                csv.append(",");
            }
        }
        csv.append("\n");
        
        // Data
        for (T entity : data) {
            for (int i = 0; i < fields.length; i++) {
                fields[i].setAccessible(true);
                try {
                    Object value = fields[i].get(entity);
                    csv.append("\"").append(formatValueForCsv(value)).append("\"");
                } catch (Exception e) {
                    // log.error("Error al acceder al campo: " + fields[i].getName(), e);
                    csv.append("\"\"");
                }
                
                if (i < fields.length - 1) {
                    csv.append(",");
                }
            }
            csv.append("\n");
        }
        
        return csv.toString();
    }
    
    /**
     * Exports a report to PDF format
     * @param <T> data type of the report
     * @param report the report to export
     * @param reportTitle title of the report
     * @return byte array with PDF content
     */
    public <T> byte[] exportToPdf(ReportResponseDTO<T> report, String reportTitle) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Add title
            Paragraph title = new Paragraph(reportTitle)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold();
            document.add(title);
            
            // Add generation date
            Paragraph dateInfo = new Paragraph("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setMarginBottom(20);
            document.add(dateInfo);
            
            // Add report summary if available
            if (report.getMessage() != null && !report.getMessage().isEmpty()) {
                Paragraph summary = new Paragraph("Summary: " + report.getMessage())
                    .setFontSize(12)
                    .setMarginBottom(15);
                document.add(summary);
            }
            
            // Add total elements info
            if (report.getTotalElements() > 0) {
                Paragraph totalInfo = new Paragraph("Total Records: " + report.getTotalElements())
                    .setFontSize(10)
                    .setMarginBottom(15);
                document.add(totalInfo);
            }
            
            List<T> data = report.getData();
            if (data == null || data.isEmpty()) {
                Paragraph noData = new Paragraph("No data available for this report")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12);
                document.add(noData);
                document.close();
                return outputStream.toByteArray();
            }
            
            // Create table with data
            T firstEntity = data.get(0);
            Field[] fields = firstEntity.getClass().getDeclaredFields();
            
            // Create table with proper column count
            Table table = new Table(UnitValue.createPercentArray(fields.length)).useAllAvailableWidth();
            
            // Add headers
            for (Field field : fields) {
                Cell headerCell = new Cell()
                    .add(new Paragraph(formatFieldName(field.getName()))
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER))
                    .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY);
                table.addHeaderCell(headerCell);
            }
            
            // Add data rows
            for (T entity : data) {
                for (Field field : fields) {
                    field.setAccessible(true);
                    try {
                        Object value = field.get(entity);
                        String cellValue = formatValueForPdf(value);
                        Cell dataCell = new Cell()
                            .add(new Paragraph(cellValue)
                            .setFontSize(9))
                            .setTextAlignment(TextAlignment.LEFT);
                        table.addCell(dataCell);
                    } catch (Exception e) {
                        Cell errorCell = new Cell()
                            .add(new Paragraph("N/A")
                            .setFontSize(9))
                            .setTextAlignment(TextAlignment.CENTER);
                        table.addCell(errorCell);
                    }
                }
            }
            
            document.add(table);
            
            // Add additional data if available
            if (report.getAdditionalData() != null) {
                document.add(new AreaBreak());
                addAdditionalDataToPdf(document, report.getAdditionalData());
            }
            
            // Add metadata if available
            if (report.getMetadata() != null) {
                document.add(new AreaBreak());
                addMetadataToPdf(document, report.getMetadata());
            }
            
            document.close();
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            // log.error("Error al exportar a PDF", e);
            throw new RuntimeException("Error generating PDF file", e);
        }
    }
    
    /**
     * Formats a value for PDF display
     */
    private String formatValueForPdf(Object value) {
        if (value == null) {
            return "";
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } else if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) {
                return "N/A";
            }
            return list.size() + " items";
        } else {
            String stringValue = value.toString();
            // Truncate very long strings
            if (stringValue.length() > 50) {
                return stringValue.substring(0, 47) + "...";
            }
            return stringValue;
        }
    }
    
    /**
     * Adds additional data section to PDF
     */
    private void addAdditionalDataToPdf(Document document, Object additionalData) {
        try {
            Paragraph additionalTitle = new Paragraph("Additional Statistics")
                .setFontSize(14)
                .setBold()
                .setMarginTop(20);
            document.add(additionalTitle);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = objectMapper.convertValue(additionalData, Map.class);
            
            Table additionalTable = new Table(2).useAllAvailableWidth();
            
            // Headers
            additionalTable.addHeaderCell(new Cell()
                .add(new Paragraph("Metric").setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            additionalTable.addHeaderCell(new Cell()
                .add(new Paragraph("Value").setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            
            // Data
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                additionalTable.addCell(new Cell().add(new Paragraph(formatFieldName(entry.getKey()))));
                additionalTable.addCell(new Cell().add(new Paragraph(formatValueForPdf(entry.getValue()))));
            }
            
            document.add(additionalTable);
            
        } catch (Exception e) {
            document.add(new Paragraph("Error loading additional data: " + e.getMessage())
                .setFontSize(10)
                .setItalic());
        }
    }
    
    /**
     * Adds metadata section to PDF
     */
    private void addMetadataToPdf(Document document, Object metadata) {
        try {
            Paragraph metadataTitle = new Paragraph("Report Metadata")
                .setFontSize(14)
                .setBold()
                .setMarginTop(20);
            document.add(metadataTitle);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> metadataMap = objectMapper.convertValue(metadata, Map.class);
            
            Table metadataTable = new Table(2).useAllAvailableWidth();
            
            // Headers
            metadataTable.addHeaderCell(new Cell()
                .add(new Paragraph("Property").setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            metadataTable.addHeaderCell(new Cell()
                .add(new Paragraph("Value").setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            
            // Data
            for (Map.Entry<String, Object> entry : metadataMap.entrySet()) {
                metadataTable.addCell(new Cell().add(new Paragraph(formatFieldName(entry.getKey()))));
                metadataTable.addCell(new Cell().add(new Paragraph(formatValueForPdf(entry.getValue()))));
            }
            
            document.add(metadataTable);
            
        } catch (Exception e) {
            document.add(new Paragraph("Error loading metadata: " + e.getMessage())
                .setFontSize(10)
                .setItalic());
        }
    }
    
    /**
     * Formats a field name to display as a column header
     */
    private String formatFieldName(String fieldName) {
        String[] words = fieldName.split("(?=\\p{Upper})");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            result.append(word).append(" ");
        }
        return result.toString().trim();
    }
    
    /**
     * Assigns a value to an Excel cell based on its type
     */
    private void setCellValueBasedOnType(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof LocalDate) {
            cell.setCellValue(((LocalDate) value).format(DateTimeFormatter.ISO_LOCAL_DATE));
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue(((LocalDateTime) value).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } else {
            cell.setCellValue(value.toString());
        }
    }
    
    /**
     * Formats a value to include in a CSV
     */
    private String formatValueForCsv(Object value) {
        if (value == null) {
            return "";
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).format(DateTimeFormatter.ISO_LOCAL_DATE);
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else {
            return value.toString().replace("\"", "\"\"");
        }
    }
    
    /**
     * Creates a summary sheet with additional data
     */
    private void createSummarySheet(Sheet sheet, Object datosAdicionales, CellStyle headerStyle) {
        try {
            // Convert additional data to a map for easier handling
            @SuppressWarnings("unchecked")
            Map<String, Object> summaryData = objectMapper.convertValue(datosAdicionales, Map.class);
            
            int rowNum = 0;
            
            // Create header
            Row headerRow = sheet.createRow(rowNum++);
            Cell keyCell = headerRow.createCell(0);
            keyCell.setCellValue("Indicator");
            keyCell.setCellStyle(headerStyle);
            
            Cell valueCell = headerRow.createCell(1);
            valueCell.setCellValue("Value");
            valueCell.setCellStyle(headerStyle);
            
            // Fill with data
            for (Map.Entry<String, Object> entry : summaryData.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(formatFieldName(entry.getKey()));
                
                Cell cell = row.createCell(1);
                setCellValueBasedOnType(cell, entry.getValue());
            }
            
            // Adjust columns
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            
        } catch (Exception e) {
            // log.error("Error al crear hoja de resumen", e);
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("Error creating summary: " + e.getMessage());
        }
    }
    
    /**
     * Creates a metadata sheet for the report
     */
    private void createMetadataSheet(Sheet sheet, Object metadata, CellStyle headerStyle) {
        try {
            // Convert metadata to a map for easier handling
            @SuppressWarnings("unchecked")
            Map<String, Object> metadataMap = objectMapper.convertValue(metadata, Map.class);
            
            int rowNum = 0;
            
            // Create header
            Row headerRow = sheet.createRow(rowNum++);
            Cell keyCell = headerRow.createCell(0);
            keyCell.setCellValue("Property");
            keyCell.setCellStyle(headerStyle);
            
            Cell valueCell = headerRow.createCell(1);
            valueCell.setCellValue("Value");
            valueCell.setCellStyle(headerStyle);
            
            // Fill with data
            for (Map.Entry<String, Object> entry : metadataMap.entrySet()) {
                if (entry.getKey().equals("appliedFilters")) {
                    // Handle applied filters as a special case
                    @SuppressWarnings("unchecked")
                    Map<String, Object> filters = (Map<String, Object>) entry.getValue();
                    
                    // Create a sub-section for filters
                    Row filtersHeaderRow = sheet.createRow(rowNum++);
                    Cell filtersCell = filtersHeaderRow.createCell(0);
                    filtersCell.setCellValue("Applied Filters");
                    filtersCell.setCellStyle(headerStyle);
                    
                    // Add each filter
                    for (Map.Entry<String, Object> filter : filters.entrySet()) {
                        if (filter.getValue() != null) {
                            Row filterRow = sheet.createRow(rowNum++);
                            filterRow.createCell(0).setCellValue("   " + formatFieldName(filter.getKey()));
                            
                            Cell filterValueCell = filterRow.createCell(1);
                            setCellValueBasedOnType(filterValueCell, filter.getValue());
                        }
                    }
                } else {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(formatFieldName(entry.getKey()));
                    
                    Cell cell = row.createCell(1);
                    setCellValueBasedOnType(cell, entry.getValue());
                }
            }
            
            // Adjust columns
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            
        } catch (Exception e) {
            // log.error("Error al crear hoja de metadata", e);
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("Error creating metadata: " + e.getMessage());
        }
    }
}