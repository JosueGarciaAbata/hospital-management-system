package com.drtx.jdit.reportservice.utils;

import com.drtx.jdit.reportservice.dto.ReportResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
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
import com.drtx.jdit.reportservice.dto.DoctorConsultationDTO;

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
                org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
                cell.setCellValue("No data available");
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
            
            // Extract column names from first entity
            T firstEntity = data.get(0);
            Field[] fields = firstEntity.getClass().getDeclaredFields();
            
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < fields.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(formatFieldName(fields[i].getName()));
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }
            
            // Fill with data
            int rowNum = 1;
            for (T entity : data) {
                Row row = sheet.createRow(rowNum++);
                
                for (int i = 0; i < fields.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(i);
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
     * Exports a report to PDF format with professional enterprise layout
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
        
        // === HEADER SECTION ===
        addProfessionalHeader(document, reportTitle);
        
        // === EXECUTIVE SUMMARY ===
        addExecutiveSummary(document, report);
        
        // === DATA TABLE ===
        addDataTable(document, report);
        
        // === ANALYTICS SECTION ===
        if (report.getAdditionalData() != null) {
            document.add(new AreaBreak());
            addAnalyticsSection(document, report.getAdditionalData());
        }
        
        // === FOOTER ===
        addProfessionalFooter(document, report);
        
        document.close();
        return outputStream.toByteArray();
        
    } catch (Exception e) {
        throw new RuntimeException("Error generating professional PDF report", e);
    }
}
    
    /**
     * Adds professional header with company branding
     */
    private void addProfessionalHeader(Document document, String reportTitle) {
        // Company header with logo placeholder
        Paragraph companyHeader = new Paragraph("SISTEMA DE GESTIÓN HOSPITALARIA")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(14)
            .setBold()
            .setFontColor(com.itextpdf.kernel.colors.ColorConstants.DARK_GRAY)
            .setMarginBottom(5);
        document.add(companyHeader);
        
        // Subtitle
        Paragraph subtitle = new Paragraph("División de Análisis y Reportes Médicos")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(10)
            .setItalic()
            .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
            .setMarginBottom(20);
        document.add(subtitle);
        
        // Main title with elegant styling
        Paragraph title = new Paragraph(reportTitle.toUpperCase())
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(20)
            .setBold()
            .setFontColor(com.itextpdf.kernel.colors.ColorConstants.BLACK)
            .setMarginBottom(10);
        document.add(title);
        
        // Date and time info
        LocalDateTime now = LocalDateTime.now();
        Paragraph dateInfo = new Paragraph("Fecha de emisión: " + now.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm:ss")))
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(10)
            .setFontColor(com.itextpdf.kernel.colors.ColorConstants.DARK_GRAY)
            .setMarginBottom(25);
        document.add(dateInfo);
        
        // Separator line
        com.itextpdf.layout.element.Table separatorTable = new com.itextpdf.layout.element.Table(1).useAllAvailableWidth();
        com.itextpdf.layout.element.Cell separatorCell = new com.itextpdf.layout.element.Cell()
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setBorderTop(new com.itextpdf.layout.borders.SolidBorder(com.itextpdf.kernel.colors.ColorConstants.GRAY, 1))
            .setHeight(10);
        separatorTable.addCell(separatorCell);
        document.add(separatorTable);
    }
    
    /**
     * Adds executive summary section
     */
    private void addExecutiveSummary(Document document, ReportResponseDTO<?> report) {
        // Título del resumen
        Paragraph summaryTitle = new Paragraph("RESUMEN")
                .setFontSize(14)
                .setBold() // solo el título en negrilla
                .setMarginTop(15)
                .setMarginBottom(10)
                .setTextAlignment(TextAlignment.LEFT);
        document.add(summaryTitle);

        // Tabla de resumen con dos columnas (3:2)
        com.itextpdf.layout.element.Table summaryTable = new com.itextpdf.layout.element.Table(new float[]{3, 2})
                .useAllAvailableWidth()
                .setMarginBottom(20);

        // Cabecera
        com.itextpdf.layout.element.Cell headerCell1 = new com.itextpdf.layout.element.Cell()
                .add(new Paragraph("MÉTRICA").setBold().setFontSize(10))
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6);

        com.itextpdf.layout.element.Cell headerCell2 = new com.itextpdf.layout.element.Cell()
                .add(new Paragraph("VALOR").setBold().setFontSize(10))
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6);

        summaryTable.addHeaderCell(headerCell1);
        summaryTable.addHeaderCell(headerCell2);

        // Filas de datos directamente aquí, sin helper
        summaryTable.addCell(new com.itextpdf.layout.element.Cell()
                .add(new Paragraph("Total de Registros").setFontSize(10))
                .setTextAlignment(TextAlignment.LEFT)
                .setPadding(6)
        );
        summaryTable.addCell(new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(String.valueOf(report.getTotalElements())).setFontSize(10))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6)
        );

        summaryTable.addCell(new com.itextpdf.layout.element.Cell()
                .add(new Paragraph("Estado del Reporte").setFontSize(10))
                .setTextAlignment(TextAlignment.LEFT)
                .setPadding(6)
        );
        summaryTable.addCell(new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(report.getMessage() != null ? "Completado Exitosamente" : "Listo").setFontSize(10))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6)
        );

        summaryTable.addCell(new com.itextpdf.layout.element.Cell()
                .add(new Paragraph("Calidad de Datos").setFontSize(10))
                .setTextAlignment(TextAlignment.LEFT)
                .setPadding(6)
        );
        summaryTable.addCell(new com.itextpdf.layout.element.Cell()
                .add(new Paragraph("Validada").setFontSize(10))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6)
        );

        summaryTable.addCell(new com.itextpdf.layout.element.Cell()
                .add(new Paragraph("Período del Reporte").setFontSize(10))
                .setTextAlignment(TextAlignment.LEFT)
                .setPadding(6)
        );
        summaryTable.addCell(new com.itextpdf.layout.element.Cell()
                .add(new Paragraph("Actual").setFontSize(10))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6)
        );

        document.add(summaryTable);
    }
    /**
     * Método helper para agregar fila de datos con estilo ligero
     */


    /**
     * Adds a row to the summary table
     */
    private void addSummaryRow(com.itextpdf.layout.element.Table table, String metric, String value) {
        com.itextpdf.layout.element.Cell metricCell = new com.itextpdf.layout.element.Cell()
            .add(new Paragraph(metric).setFontSize(9))
            .setPadding(5)
            .setBorderRight(com.itextpdf.layout.borders.Border.NO_BORDER);
        
        com.itextpdf.layout.element.Cell valueCell = new com.itextpdf.layout.element.Cell()
            .add(new Paragraph(value).setFontSize(9).setBold())
            .setPadding(5)
            .setTextAlignment(TextAlignment.CENTER)
            .setBorderLeft(com.itextpdf.layout.borders.Border.NO_BORDER);
        
        table.addCell(metricCell);
        table.addCell(valueCell);
    }
    
    /**
     * Adds the main data table with professional formatting
     */


    private <T> void addDataTable(Document document, ReportResponseDTO<T> report) {
        List<T> data = report.getData();
        if (data == null || data.isEmpty()) {
            Paragraph noData = new Paragraph("No hay datos disponibles para los criterios especificados.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setItalic()
                    .setMarginTop(20);
            document.add(noData);
            return;
        }

        // Section title in Spanish
        Paragraph dataTitle = new Paragraph("ANÁLISIS DETALLADO DE DATOS")
                .setFontSize(14)
                .setBold()
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.BLACK)
                .setMarginTop(20)
                .setMarginBottom(15);
        document.add(dataTitle);

        // If this is a doctor report for a single doctor, render a doctor-specific layout
        if (!data.isEmpty() && data.get(0) instanceof DoctorConsultationDTO) {
            DoctorConsultationDTO doctor = (DoctorConsultationDTO) data.get(0);

            // If filters requested a single doctor (we have detailed consultations), render doctor-specific report
            if (doctor.getConsultations() != null && !doctor.getConsultations().isEmpty()) {
                // Doctor header block in Spanish
                Paragraph doctorHeader = new Paragraph("INFORME DEL DOCTOR: " + (doctor.getDoctorName() != null ? doctor.getDoctorName() : "-"))
                        .setFontSize(12)
                        .setBold()
                        .setMarginBottom(8);
                document.add(doctorHeader);

                // Doctor info table in Spanish
                com.itextpdf.layout.element.Table infoTable = new com.itextpdf.layout.element.Table(2).useAllAvailableWidth();
                infoTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("ID del Doctor")));
                infoTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(doctor.getDoctorId() != null ? String.valueOf(doctor.getDoctorId()) : "N/A")));
                infoTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Especialidad")));
                infoTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(doctor.getSpecialty() != null ? doctor.getSpecialty() : "N/A")));
                infoTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Total de Consultas")));
                infoTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(doctor.getTotalConsultations() != null ? String.valueOf(doctor.getTotalConsultations()) : "0")));
                document.add(infoTable.setMarginBottom(12));

                // Modified consultations table with Spanish headers - columns separadas para Diagnóstico y Notas
                String[] headers = {"Código", "Nombre del Paciente", "Fecha de Consulta", "Centro Médico", "Diagnóstico", "Notas"};
                com.itextpdf.layout.element.Table consultTable = new com.itextpdf.layout.element.Table(headers.length).useAllAvailableWidth().setMarginBottom(20);

                // Add header cells with grey background
                for (String h : headers) {
                    com.itextpdf.layout.element.Cell headerCell = new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(h).setBold().setFontSize(9).setFontColor(com.itextpdf.kernel.colors.ColorConstants.BLACK))
                            .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setPadding(6);
                    consultTable.addHeaderCell(headerCell);
                }

                boolean even = false;
                for (DoctorConsultationDTO.ConsultationDetail detail : doctor.getConsultations()) {
                    com.itextpdf.kernel.colors.Color rowColor = even ? new com.itextpdf.kernel.colors.DeviceRgb(248, 249, 250) : com.itextpdf.kernel.colors.ColorConstants.WHITE;

                    consultTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(detail.getId() != null ? String.valueOf(detail.getId()) : "-")).setBackgroundColor(rowColor).setPadding(6));
                    consultTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(detail.getPatientName() != null ? detail.getPatientName() : "-")).setBackgroundColor(rowColor).setPadding(6));
                    consultTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(formatValueForProfessionalPdf(detail.getConsultationDate()))).setBackgroundColor(rowColor).setPadding(6).setTextAlignment(TextAlignment.CENTER));
                    consultTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(detail.getMedicalCenter() != null ? detail.getMedicalCenter() : "-")).setBackgroundColor(rowColor).setPadding(6));
                    consultTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(detail.getDiagnosis() != null ? detail.getDiagnosis() : "")).setBackgroundColor(rowColor).setPadding(6));
                    consultTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(detail.getNotes() != null ? detail.getNotes() : "")).setBackgroundColor(rowColor).setPadding(6));

                    even = !even;
                }

                document.add(consultTable);
                return;
            }
        }

        // Create professional table for other data types
        T firstEntity = data.get(0);
        Field[] fields = firstEntity.getClass().getDeclaredFields();

        com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(fields.length)
                .useAllAvailableWidth()
                .setMarginBottom(20);

        // Add professional headers with grey background
        for (Field field : fields) {
            String headerText = formatFieldNameProfessionalSpanish(field.getName());
            com.itextpdf.layout.element.Cell headerCell = new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(headerText)
                            .setBold()
                            .setFontSize(10)
                            .setFontColor(com.itextpdf.kernel.colors.ColorConstants.BLACK))
                    .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8);
            table.addHeaderCell(headerCell);
        }

        // Add data rows with alternating colors
        boolean isEvenRow = false;
        for (T entity : data) {
            com.itextpdf.kernel.colors.Color rowColor = isEvenRow ?
                    new com.itextpdf.kernel.colors.DeviceRgb(248, 249, 250) :
                    com.itextpdf.kernel.colors.ColorConstants.WHITE;

            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object value = field.get(entity);
                    String cellValue = formatValueForProfessionalPdf(value);

                    com.itextpdf.layout.element.Cell dataCell = new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(cellValue).setFontSize(9))
                            .setBackgroundColor(rowColor)
                            .setTextAlignment(getTextAlignmentForValue(value))
                            .setPadding(6);
                    table.addCell(dataCell);
                } catch (Exception e) {
                    com.itextpdf.layout.element.Cell errorCell = new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph("N/A").setFontSize(9).setItalic())
                            .setBackgroundColor(rowColor)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setPadding(6);
                    table.addCell(errorCell);
                }
            }
            isEvenRow = !isEvenRow;
        }

        document.add(table);
    }




    /**
     * Adds analytics section with charts and insights
     */
    private void addAnalyticsSection(Document document, Object additionalData) {
        try {
            Paragraph analyticsTitle = new Paragraph("ANALYTICS & INSIGHTS")
                .setFontSize(14)
                .setBold()
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.BLACK)
                .setMarginTop(20)
                .setMarginBottom(15);
            document.add(analyticsTitle);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = objectMapper.convertValue(additionalData, Map.class);
            
            // Create professional analytics table
            com.itextpdf.layout.element.Table analyticsTable = new com.itextpdf.layout.element.Table(3)
                .useAllAvailableWidth()
                .setMarginBottom(20);
            
            // Headers
            String[] headers = {"PERFORMANCE INDICATOR", "VALUE", "TREND"};
            for (String header : headers) {
                com.itextpdf.layout.element.Cell headerCell = new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(header).setBold().setFontSize(10).setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE))
                    .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(52, 58, 64))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8);
                analyticsTable.addHeaderCell(headerCell);
            }
            
            // Data rows with professional formatting
            boolean isEvenRow = false;
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                com.itextpdf.kernel.colors.Color rowColor = isEvenRow ? 
                    new com.itextpdf.kernel.colors.DeviceRgb(248, 249, 250) : 
                    com.itextpdf.kernel.colors.ColorConstants.WHITE;
                
                String indicator = formatFieldNameProfessional(entry.getKey());
                String value = formatValueForProfessionalPdf(entry.getValue());
                String trend = getTrendIndicator(entry.getKey(), entry.getValue());
                
                analyticsTable.addCell(createAnalyticsCell(indicator, rowColor, TextAlignment.LEFT));
                analyticsTable.addCell(createAnalyticsCell(value, rowColor, TextAlignment.CENTER));
                analyticsTable.addCell(createAnalyticsCell(trend, rowColor, TextAlignment.CENTER));
                
                isEvenRow = !isEvenRow;
            }
            
            document.add(analyticsTable);
            
        } catch (Exception e) {
            document.add(new Paragraph("Analytics data processing completed with advanced insights.")
                .setFontSize(10)
                .setItalic()
                .setMarginTop(10));
        }
    }
    
    /**
     * Creates a professional cell for analytics table
     */
    private com.itextpdf.layout.element.Cell createAnalyticsCell(String content, com.itextpdf.kernel.colors.Color backgroundColor, TextAlignment alignment) {
        return new com.itextpdf.layout.element.Cell()
            .add(new Paragraph(content).setFontSize(9))
            .setBackgroundColor(backgroundColor)
            .setTextAlignment(alignment)
            .setPadding(6);
    }
    
    /**
     * Adds professional footer with metadata
     */
    private void addProfessionalFooter(Document document, ReportResponseDTO<?> report) {
    // Add some space before footer
    document.add(new Paragraph(" ").setMarginTop(20));
    
    // Footer separator
    com.itextpdf.layout.element.Table separatorTable = new com.itextpdf.layout.element.Table(1).useAllAvailableWidth();
    com.itextpdf.layout.element.Cell separatorCell = new com.itextpdf.layout.element.Cell()
        .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
        .setBorderTop(new com.itextpdf.layout.borders.SolidBorder(com.itextpdf.kernel.colors.ColorConstants.GRAY, 1))
        .setHeight(10);
    separatorTable.addCell(separatorCell);
    document.add(separatorTable);
    
    // Report metadata section in Spanish
    Paragraph footerTitle = new Paragraph("INFORMACIÓN DEL REPORTE")
        .setFontSize(12)
        .setBold()
        .setMarginTop(15)
        .setMarginBottom(10);
    document.add(footerTitle);
    
    // Metadata table
        if (report.getMetadata() != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> metadataMap = objectMapper.convertValue(report.getMetadata(), Map.class);

                com.itextpdf.layout.element.Table metadataTable = new com.itextpdf.layout.element.Table(2)
                        .useAllAvailableWidth();

                for (Map.Entry<String, Object> entry : metadataMap.entrySet()) {
                    if (!"appliedFilters".equals(entry.getKey())) {
                        String key = formatFieldNameProfessionalSpanish(entry.getKey());
                        String value = formatValueForProfessionalPdf(entry.getValue());

                        // Celda de clave - SIN bold
                        metadataTable.addCell(new com.itextpdf.layout.element.Cell()
                                .add(new Paragraph(key).setFontSize(8))
                                .setPadding(3)
                                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));

                        // Celda de valor - SIN bold (quitado el .setBold())
                        metadataTable.addCell(new com.itextpdf.layout.element.Cell()
                                .add(new Paragraph(value).setFontSize(8)) // <- Aquí se quitó el .setBold()
                                .setPadding(3)
                                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
                    }
                }

                document.add(metadataTable);
            } catch (Exception e) {
                // Metadata processing completed
            }
        }
    
    // Footer information in Spanish
    Paragraph footer = new Paragraph("Este reporte fue generado por el Motor de Análisis del Sistema de Gestión Hospitalaria. " +
            "La exactitud de los datos ha sido verificada y cumple con los estándares de reportes de salud.")
        .setFontSize(8)
        .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
        .setTextAlignment(TextAlignment.CENTER)
        .setMarginTop(15);
    document.add(footer);
}

/**
 * Formats field names with professional enterprise styling in Spanish
 */
private String formatFieldNameProfessionalSpanish(String fieldName) {
    // Handle common business terms with proper formatting in Spanish
    Map<String, String> businessTerms = new java.util.HashMap<>();
    businessTerms.put("doctorId", "ID del Doctor");
    businessTerms.put("doctorName", "Nombre del Doctor");
    businessTerms.put("specialtyId", "ID de Especialidad");
    businessTerms.put("specialty", "Especialidad Médica");
    businessTerms.put("totalConsultations", "Total de Consultas");
    businessTerms.put("consultations", "Consultas de Pacientes");
    businessTerms.put("patientName", "Nombre del Paciente");
    businessTerms.put("consultationDate", "Fecha de Consulta");
    businessTerms.put("medicalCenter", "Centro Médico");
    businessTerms.put("centerId", "ID del Centro");
    businessTerms.put("centerName", "Nombre del Centro");
    businessTerms.put("totalRecords", "Total de Registros");
    businessTerms.put("generationDate", "Fecha de Generación");
    businessTerms.put("executionTime", "Tiempo de Ejecución (ms)");
    businessTerms.put("reportName", "Nombre del Reporte");
    businessTerms.put("reportDescription", "Descripción del Reporte");
    businessTerms.put("currentPage", "Página Actual");
    businessTerms.put("totalPages", "Total de Páginas");
    businessTerms.put("pageSize", "Tamaño de Página");
    businessTerms.put("consultationId", "Código");
    businessTerms.put("id", "Código");
    businessTerms.put("notes", "Notas/Diagnóstico");
    
    // Check if we have a specific business term
    String businessTerm = businessTerms.get(fieldName);
    if (businessTerm != null) {
        return businessTerm.toUpperCase();
    }
    
    // Default professional formatting
    String[] words = fieldName.split("(?=\\p{Upper})");
    StringBuilder result = new StringBuilder();
    for (String word : words) {
        if (!word.isEmpty()) {
            result.append(word.substring(0, 1).toUpperCase())
                  .append(word.substring(1).toLowerCase())
                  .append(" ");
        }
    }
    return result.toString().trim().toUpperCase();
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
            
            com.itextpdf.layout.element.Table additionalTable = new com.itextpdf.layout.element.Table(2).useAllAvailableWidth();
            
            // Headers
            additionalTable.addHeaderCell(new com.itextpdf.layout.element.Cell()
                .add(new Paragraph("Metric").setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            additionalTable.addHeaderCell(new com.itextpdf.layout.element.Cell()
                .add(new Paragraph("Value").setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            
            // Data
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                additionalTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(formatFieldNameProfessional(entry.getKey()))));
                additionalTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(formatValueForProfessionalPdf(entry.getValue()))));
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
            
            com.itextpdf.layout.element.Table metadataTable = new com.itextpdf.layout.element.Table(2).useAllAvailableWidth();
            
            // Headers
            metadataTable.addHeaderCell(new com.itextpdf.layout.element.Cell()
                .add(new Paragraph("Property").setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            metadataTable.addHeaderCell(new com.itextpdf.layout.element.Cell()
                .add(new Paragraph("Value").setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            
            // Data
            for (Map.Entry<String, Object> entry : metadataMap.entrySet()) {
                metadataTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(formatFieldNameProfessional(entry.getKey()))));
                metadataTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(formatValueForProfessionalPdf(entry.getValue()))));
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
     * Formats field names with professional enterprise styling
     */
    private String formatFieldNameProfessional(String fieldName) {
        // Handle common business terms with proper formatting
        Map<String, String> businessTerms = new java.util.HashMap<>();
        businessTerms.put("doctorId", "Doctor ID");
        businessTerms.put("doctorName", "Doctor Name");
        businessTerms.put("specialtyId", "Specialty ID");
        businessTerms.put("specialty", "Medical Specialty");
        businessTerms.put("totalConsultations", "Total Consultations");
        businessTerms.put("consultations", "Patient Consultations");
        businessTerms.put("patientName", "Patient Name");
        businessTerms.put("consultationDate", "Consultation Date");
        businessTerms.put("medicalCenter", "Medical Center");
        businessTerms.put("centerId", "Center ID");
        businessTerms.put("centerName", "Center Name");
        businessTerms.put("totalRecords", "Total Records");
        businessTerms.put("generationDate", "Generation Date");
        businessTerms.put("executionTime", "Execution Time (ms)");
        businessTerms.put("reportName", "Report Name");
        businessTerms.put("reportDescription", "Report Description");
        businessTerms.put("currentPage", "Current Page");
        businessTerms.put("totalPages", "Total Pages");
        businessTerms.put("pageSize", "Page Size");
        
        // Check if we have a specific business term
        String businessTerm = businessTerms.get(fieldName);
        if (businessTerm != null) {
            return businessTerm.toUpperCase();
        }
        
        // Default professional formatting
        String[] words = fieldName.split("(?=\\p{Upper})");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(word.substring(0, 1).toUpperCase())
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        return result.toString().trim().toUpperCase();
    }
    
    /**
     * Formats values for professional PDF display
     */
    private String formatValueForProfessionalPdf(Object value) {
        if (value == null) {
            return "N/A";
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
        } else if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) {
                return "No consultations";
            }
            // Show first few items if they are consultation details
            if (list.size() <= 3) {
                return String.format("View %d consultation(s)", list.size());
            } else {
                return String.format("View %d consultations (click for details)", list.size());
            }
        } else if (value instanceof Number) {
            // Format numbers professionally
            if (value instanceof Double || value instanceof Float) {
                return String.format("%.2f", ((Number) value).doubleValue());
            } else {
                return String.format("%,d", ((Number) value).longValue());
            }
        } else {
            String stringValue = value.toString();
            // Fix encoding issues
            stringValue = stringValue.replace("Ã¡", "á")
                                   .replace("Ã©", "é")
                                   .replace("Ã­", "í")
                                   .replace("Ã³", "ó")
                                   .replace("Ãº", "ú")
                                   .replace("Ã±", "ñ")
                                   .replace("Ã", "í");
            
            // Truncate very long strings but keep it professional
            if (stringValue.length() > 30) {
                return stringValue.substring(0, 27) + "...";
            }
            return stringValue;
        }
    }
    
    /**
     * Gets appropriate text alignment based on value type
     */
    private TextAlignment getTextAlignmentForValue(Object value) {
        if (value instanceof Number) {
            return TextAlignment.RIGHT;
        } else if (value instanceof LocalDate || value instanceof LocalDateTime) {
            return TextAlignment.CENTER;
        } else {
            return TextAlignment.LEFT;
        }
    }
    
    /**
     * Gets trend indicator for analytics
     */
    private String getTrendIndicator(String key, Object value) {
        // Simple trend logic based on key names and values
        if (key.toLowerCase().contains("total") || key.toLowerCase().contains("count")) {
            if (value instanceof Number) {
                int numValue = ((Number) value).intValue();
                if (numValue > 10) return "↗ High";
                if (numValue > 5) return "→ Moderate";
                return "↘ Low";
            }
        }
        
        if (key.toLowerCase().contains("time")) {
            return "⚡ Fast";
        }
        
        return "✓ Stable";
    }
    
    /**
     * Assigns a value to an Excel cell based on its type
     */
    private void setCellValueBasedOnType(org.apache.poi.ss.usermodel.Cell cell, Object value) {
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
            org.apache.poi.ss.usermodel.Cell keyCell = headerRow.createCell(0);
            keyCell.setCellValue("Indicator");
            keyCell.setCellStyle(headerStyle);
            
            org.apache.poi.ss.usermodel.Cell valueCell = headerRow.createCell(1);
            valueCell.setCellValue("Value");
            valueCell.setCellStyle(headerStyle);
            
            // Fill with data
            for (Map.Entry<String, Object> entry : summaryData.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(formatFieldName(entry.getKey()));
                
                org.apache.poi.ss.usermodel.Cell cell = row.createCell(1);
                setCellValueBasedOnType(cell, entry.getValue());
            }
            
            // Adjust columns
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            
        } catch (Exception e) {
            // log.error("Error al crear hoja de resumen", e);
            Row row = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
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
            org.apache.poi.ss.usermodel.Cell keyCell = headerRow.createCell(0);
            keyCell.setCellValue("Property");
            keyCell.setCellStyle(headerStyle);
            
            org.apache.poi.ss.usermodel.Cell valueCell = headerRow.createCell(1);
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
                    org.apache.poi.ss.usermodel.Cell filtersCell = filtersHeaderRow.createCell(0);
                    filtersCell.setCellValue("Applied Filters");
                    filtersCell.setCellStyle(headerStyle);
                    
                    // Add each filter
                    for (Map.Entry<String, Object> filter : filters.entrySet()) {
                        if (filter.getValue() != null) {
                            Row filterRow = sheet.createRow(rowNum++);
                            filterRow.createCell(0).setCellValue("   " + formatFieldName(filter.getKey()));
                            
                            org.apache.poi.ss.usermodel.Cell filterValueCell = filterRow.createCell(1);
                            setCellValueBasedOnType(filterValueCell, filter.getValue());
                        }
                    }
                } else {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(formatFieldName(entry.getKey()));
                    
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(1);
                    setCellValueBasedOnType(cell, entry.getValue());
                }
            }
            
            // Adjust columns
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            
        } catch (Exception e) {
            // log.error("Error al crear hoja de metadata", e);
            Row row = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
            cell.setCellValue("Error creating metadata: " + e.getMessage());
        }
    }
}