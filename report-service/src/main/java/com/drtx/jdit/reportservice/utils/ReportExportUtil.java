package com.drtx.jdit.reportservice.utils;

import com.drtx.jdit.reportservice.dto.ReportResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@Slf4j
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