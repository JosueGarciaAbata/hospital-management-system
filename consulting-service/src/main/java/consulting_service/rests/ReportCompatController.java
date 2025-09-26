package consulting_service.rests;

import consulting_service.dtos.*;
import consulting_service.entities.MedicalConsultation;
import consulting_service.feign.admin_service.dtos.DoctorRead;
import consulting_service.feign.admin_service.dtos.MedicalCenterRead;
import consulting_service.feign.admin_service.services.DoctorServiceClient;
import consulting_service.feign.admin_service.services.MedicalCenterServiceClient;
import consulting_service.repositories.MedicalConsultationsRepository;
import consulting_service.repositories.PatientRepository;
import consulting_service.security.annotations.RolesAllowed;
import consulting_service.specifications.MedicalConsultationSpecifications;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador para mantener compatibilidad con endpoints de reportes
 * Este controlador existe únicamente para proporcionar datos al microservicio de reportes
 */
@RestController
@Slf4j
@RequestMapping("/api/consulting/reports")
public class ReportCompatController {

    private final MedicalConsultationsRepository consultationsRepository;
    private final PatientRepository patientRepository;
    private final DoctorServiceClient doctorServiceClient;
    private final MedicalCenterServiceClient centerServiceClient;

    @Autowired
    public ReportCompatController(
            MedicalConsultationsRepository consultationsRepository,
            PatientRepository patientRepository,
            DoctorServiceClient doctorServiceClient,
            MedicalCenterServiceClient centerServiceClient) {
        this.consultationsRepository = consultationsRepository;
        this.patientRepository = patientRepository;
        this.doctorServiceClient = doctorServiceClient;
        this.centerServiceClient = centerServiceClient;
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @PostMapping("/by-specialty")
    public ResponseEntity<List<Map<String, Object>>> getConsultasBySpecialty(
            @RequestBody SpecialtyReportRequestDTO request,
            @PageableDefault(sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Recibida solicitud para obtener consultas por especialidad: {}", request);
        try {
            // Convertir fechas
            LocalDateTime fechaInicioDateTime = request.getFechaInicio() != null ? request.getFechaInicio().atStartOfDay() : null;
            LocalDateTime fechaFinDateTime = request.getFechaFin() != null ? request.getFechaFin().atTime(LocalTime.MAX) : null;
            Boolean estadoBoolean = convertirEstado(request.getEstado());

            // Obtener consultas usando Specifications (soluciona problema PostgreSQL)
            Specification<MedicalConsultation> spec = MedicalConsultationSpecifications.withFilters(
                    fechaInicioDateTime,
                    fechaFinDateTime,
                    estadoBoolean,
                    request.getCentrosMedicos(),
                    request.getMedicos()
            );
            List<MedicalConsultation> consultas = consultationsRepository.findAll(spec, pageable).getContent();

            // ============ GENERAR REPORTE CORPORATIVO ENRIQUECIDO ============

            // 1. MÉTRICAS GENERALES - En inglés y con mejor manejo de fechas
            Map<String, Object> executiveSummary = new HashMap<>();
            executiveSummary.put("totalConsultations", consultas.size());
            executiveSummary.put("dateRangeStart", fechaInicioDateTime != null ? fechaInicioDateTime.toLocalDate() : null);
            executiveSummary.put("dateRangeEnd", fechaFinDateTime != null ? fechaFinDateTime.toLocalDate() : null);
            executiveSummary.put("reportGeneratedAt", LocalDateTime.now());
            executiveSummary.put("hasDateFilter", fechaInicioDateTime != null || fechaFinDateTime != null);

            // 2. ESTADÍSTICAS POR ESPECIALIDAD - Usando estructura de array en lugar de objeto dinámico
            Map<String, Integer> contadorEspecialidades = new HashMap<>();
            Map<String, Set<Long>> medicosUnicosPorEspecialidad = new HashMap<>();
            Map<String, Set<Long>> pacientesUnicosPorEspecialidad = new HashMap<>();

            // 3. DISTRIBUCIÓN TEMPORAL (por día de la semana) - Formato consistente en inglés
            Map<String, Integer> distribucionSemanal = new LinkedHashMap<>();
            String[] diasSemana = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
            for (String dia : diasSemana) {
                distribucionSemanal.put(dia, 0);
            }

            // 4. TOP MÉDICOS Y CENTROS
            Map<Long, Integer> consultasPorMedico = new HashMap<>();
            Map<Long, Integer> consultasPorCentro = new HashMap<>();

            // Procesar todas las consultas para generar estadísticas
            List<Map<String, Object>> consultasDetalladas = new ArrayList<>();

            for (MedicalConsultation consulta : consultas) {
                // Obtener información del médico y especialidad
                String especialidad = "No disponible";
                String nombreMedico = "Médico ID: " + consulta.getDoctorId();

                try {
                    ResponseEntity<DoctorRead> doctorResponse = doctorServiceClient.getOne(
                            consulta.getDoctorId(), false, DoctorServiceClient.ROLE);
                    if (doctorResponse.getBody() != null) {
                        DoctorRead doctor = doctorResponse.getBody();
                        nombreMedico = "Doctor " + doctor.userId();
                        if (doctor.specialtyName() != null) {
                            especialidad = doctor.specialtyName();
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error al obtener información del médico ID: {}", consulta.getDoctorId(), e);
                }

                // Acumular estadísticas
                contadorEspecialidades.merge(especialidad, 1, Integer::sum);
                medicosUnicosPorEspecialidad.computeIfAbsent(especialidad, k -> new HashSet<>()).add(consulta.getDoctorId());
                pacientesUnicosPorEspecialidad.computeIfAbsent(especialidad, k -> new HashSet<>()).add(consulta.getPatientId());

                // Distribución semanal - Consistente en inglés
                String diaSemana = consulta.getDate().getDayOfWeek().toString();
                switch (diaSemana) {
                    case "MONDAY": distribucionSemanal.merge("Monday", 1, Integer::sum); break;
                    case "TUESDAY": distribucionSemanal.merge("Tuesday", 1, Integer::sum); break;
                    case "WEDNESDAY": distribucionSemanal.merge("Wednesday", 1, Integer::sum); break;
                    case "THURSDAY": distribucionSemanal.merge("Thursday", 1, Integer::sum); break;
                    case "FRIDAY": distribucionSemanal.merge("Friday", 1, Integer::sum); break;
                    case "SATURDAY": distribucionSemanal.merge("Saturday", 1, Integer::sum); break;
                    case "SUNDAY": distribucionSemanal.merge("Sunday", 1, Integer::sum); break;
                }

                // Contadores por médico y centro
                consultasPorMedico.merge(consulta.getDoctorId(), 1, Integer::sum);
                consultasPorCentro.merge(consulta.getCenterId(), 1, Integer::sum);

                // Datos detallados de la consulta - Solo campos no-null
                String nombrePaciente = patientRepository.findById(consulta.getPatientId())
                        .map(p -> p.getFirstName() + " " + p.getLastName())
                        .orElse("Patient ID: " + consulta.getPatientId());

                Map<String, Object> consultaDetalle = new HashMap<>();
                consultaDetalle.put("consultationId", consulta.getId());
                consultaDetalle.put("specialty", especialidad);
                consultaDetalle.put("doctorId", consulta.getDoctorId());
                consultaDetalle.put("doctorName", nombreMedico);
                consultaDetalle.put("patientName", nombrePaciente);
                consultaDetalle.put("consultationDate", consulta.getDate());
                consultaDetalle.put("status", consulta.getDeleted() ? "CANCELLED" : "ACTIVE");
                consultaDetalle.put("medicalCenterId", consulta.getCenterId());

                // Solo agregar campos no-null para evitar respuesta pesada
                if (consulta.getDiagnosis() != null && !consulta.getDiagnosis().trim().isEmpty()) {
                    consultaDetalle.put("diagnosis", consulta.getDiagnosis());
                }
                if (consulta.getTreatment() != null && !consulta.getTreatment().trim().isEmpty()) {
                    consultaDetalle.put("treatment", consulta.getTreatment());
                }
                if (consulta.getNotes() != null && !consulta.getNotes().trim().isEmpty()) {
                    consultaDetalle.put("notes", consulta.getNotes());
                }

                consultasDetalladas.add(consultaDetalle);
            }

            // Generar estadísticas por especialidad como array (mejor estructura para frontends)
            List<Map<String, Object>> estadisticasPorEspecialidad = contadorEspecialidades.entrySet().stream()
                    .map(entry -> {
                        String especialidad = entry.getKey();
                        int totalConsultas = entry.getValue();
                        int medicosUnicos = medicosUnicosPorEspecialidad.get(especialidad).size();
                        int pacientesUnicos = pacientesUnicosPorEspecialidad.get(especialidad).size();
                        double promedio = Math.round((totalConsultas / (double) medicosUnicos) * 100.0) / 100.0;

                        Map<String, Object> estadistica = new HashMap<>();
                        estadistica.put("specialty", especialidad);
                        estadistica.put("totalConsultations", totalConsultas);
                        estadistica.put("uniqueDoctors", medicosUnicos);
                        estadistica.put("uniquePatients", pacientesUnicos);
                        estadistica.put("avgConsultationsPerDoctor", promedio);

                        return estadistica;
                    })
                    .sorted((a, b) -> Integer.compare((Integer) b.get("totalConsultations"), (Integer) a.get("totalConsultations")))
                    .collect(Collectors.toList());

            // Top 5 médicos más activos con información completa
            List<Map<String, Object>> topMedicos = consultasPorMedico.entrySet().stream()
                    .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                    .limit(5)
                    .map(entry -> {
                        Map<String, Object> medico = new HashMap<>();
                        medico.put("doctorId", entry.getKey());
                        medico.put("doctorName", "Doctor " + entry.getKey()); // Se podría enriquecer con más datos
                        medico.put("totalConsultations", entry.getValue());
                        medico.put("consultationShare", Math.round((entry.getValue() / (double) consultas.size() * 100.0) * 100.0) / 100.0);
                        return medico;
                    })
                    .collect(Collectors.toList());

            // RESPUESTA CORPORATIVA COMPLETA - Estructura optimizada y en inglés
            Map<String, Object> corporateReport = new HashMap<>();
            corporateReport.put("executiveSummary", executiveSummary);
            corporateReport.put("specialtyStatistics", estadisticasPorEspecialidad);
            corporateReport.put("weeklyDistribution", distribucionSemanal);
            corporateReport.put("topActiveDoctors", topMedicos);
            corporateReport.put("kpis", Map.of(
                    "distinctSpecialties", contadorEspecialidades.size(),
                    "doctorsInvolved", consultasPorMedico.size(),
                    "medicalCentersInvolved", consultasPorCentro.size(),
                    "avgConsultationsPerDoctor", Math.round((consultas.size() / (double) Math.max(consultasPorMedico.size(), 1)) * 100.0) / 100.0,
                    "dataQuality", Map.of(
                            "consultationsWithDiagnosis", (int) consultasDetalladas.stream().filter(c -> c.containsKey("diagnosis")).count(),
                            "consultationsWithTreatment", (int) consultasDetalladas.stream().filter(c -> c.containsKey("treatment")).count(),
                            "dataCompletenessPercentage", Math.round((consultasDetalladas.stream().filter(c -> c.containsKey("diagnosis") && c.containsKey("treatment")).count() / (double) consultas.size() * 100.0) * 100.0) / 100.0
                    )
            ));
            corporateReport.put("detailedConsultations", consultasDetalladas.stream().limit(20).collect(Collectors.toList()));
            corporateReport.put("totalConsultationsFound", consultas.size());

            log.info("Corporate specialty report generated successfully: {} consultations analyzed", consultas.size());

            // Configurar headers para UTF-8
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .body(List.of(corporateReport));
        } catch (Exception e) {
            log.error("Error al obtener consultas por especialidad", e);
            throw e;
        }
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @PostMapping("/by-doctor")
    public ResponseEntity<List<Map<String, Object>>> getConsultasByDoctor(
            @RequestBody DoctorReportRequestDTO request,
            @PageableDefault(sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Recibida solicitud para obtener consultas por médico: {}", request);
        try {
            // Convertir fechas
            LocalDateTime fechaInicioDateTime = request.getFechaInicio() != null ? request.getFechaInicio().atStartOfDay() : null;
            LocalDateTime fechaFinDateTime = request.getFechaFin() != null ? request.getFechaFin().atTime(LocalTime.MAX) : null;
            Boolean estadoBoolean = convertirEstado(request.getEstado());

            // Obtener todas las consultas para agrupar por médico usando Specifications
            Specification<MedicalConsultation> spec = MedicalConsultationSpecifications.withFilters(
                    fechaInicioDateTime,
                    fechaFinDateTime,
                    estadoBoolean,
                    request.getCentrosMedicos(),
                    request.getMedicos()
            );
            List<MedicalConsultation> consultas = consultationsRepository.findAll(spec, Pageable.unpaged()).getContent();

            // Agrupar consultas por médico
            Map<Long, Map<String, Object>> medicoMap = new HashMap<>();

            for (MedicalConsultation consulta : consultas) {
                Long doctorId = consulta.getDoctorId();

                if (!medicoMap.containsKey(doctorId)) {
                    Map<String, Object> medicoDto = new HashMap<>();
                    medicoDto.put("id", doctorId);

                    // Obtener información del médico
                    String especialidad = "No disponible";
                    String nombreMedico = "Médico ID: " + doctorId;
                    try {
                        ResponseEntity<DoctorRead> doctorResponse = doctorServiceClient.getOne(
                                doctorId, false, DoctorServiceClient.ROLE);
                        if (doctorResponse.getBody() != null) {
                            DoctorRead doctor = doctorResponse.getBody();
                            nombreMedico = "Doctor " + doctor.userId();
                            if (doctor.specialtyName() != null) {
                                especialidad = doctor.specialtyName();
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error al obtener información del médico ID: {}", doctorId, e);
                    }

                    medicoDto.put("nombreMedico", nombreMedico);
                    medicoDto.put("especialidad", especialidad);
                    medicoDto.put("consultas", new ArrayList<Map<String, Object>>());

                    medicoMap.put(doctorId, medicoDto);
                }

                // Agregar detalle de consulta
                Map<String, Object> detalleConsulta = new HashMap<>();
                detalleConsulta.put("consultaId", consulta.getId());

                // Obtener información del paciente
                String nombrePaciente = patientRepository.findById(consulta.getPatientId())
                        .map(p -> p.getFirstName() + " " + p.getLastName())
                        .orElse("Paciente ID: " + consulta.getPatientId());

                detalleConsulta.put("nombrePaciente", nombrePaciente);
                detalleConsulta.put("fechaConsulta", consulta.getDate());
                detalleConsulta.put("estado", consulta.getDeleted() ? "CANCELADA" : "ACTIVA");

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> consultasList = (List<Map<String, Object>>) medicoMap.get(doctorId).get("consultas");
                consultasList.add(detalleConsulta);
            }

            // Aplicar paginación manual
            List<Map<String, Object>> resultado = new ArrayList<>(medicoMap.values());

            int total = resultado.size();
            int pagina = request.getPagina() != null ? request.getPagina() : 0;
            int tamanio = request.getTamanio() != null ? request.getTamanio() : 20;
            int inicio = pagina * tamanio;
            int fin = Math.min(inicio + tamanio, total);

            if (inicio < total) {
                resultado = resultado.subList(inicio, fin);
            } else {
                resultado = new ArrayList<>();
            }

            log.info("Consultas por médico obtenidas con éxito: {} médicos", resultado.size());
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener consultas por médico", e);
            throw e;
        }
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @PostMapping("/by-center")
    public ResponseEntity<List<Map<String, Object>>> getConsultasByCenter(
            @RequestBody MedicalCenterReportRequestDTO request,
            @PageableDefault(sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Recibida solicitud para obtener consultas por centro médico: {}", request);
        try {
            // Convertir fechas
            LocalDateTime fechaInicioDateTime = request.getFechaInicio() != null ? request.getFechaInicio().atStartOfDay() : null;
            LocalDateTime fechaFinDateTime = request.getFechaFin() != null ? request.getFechaFin().atTime(LocalTime.MAX) : null;
            Boolean estadoBoolean = convertirEstado(request.getEstado());

            // Obtener todas las consultas para agrupar por centro usando Specifications
            Specification<MedicalConsultation> spec = MedicalConsultationSpecifications.withFilters(
                    fechaInicioDateTime,
                    fechaFinDateTime,
                    estadoBoolean,
                    request.getCentrosMedicos(),
                    request.getMedicos()
            );
            List<MedicalConsultation> consultas = consultationsRepository.findAll(spec, Pageable.unpaged()).getContent();

            // Agrupar consultas por centro médico
            Map<Long, Map<String, Object>> centroMap = new HashMap<>();

            for (MedicalConsultation consulta : consultas) {
                Long centerId = consulta.getCenterId();

                if (!centroMap.containsKey(centerId)) {
                    Map<String, Object> centroDto = new HashMap<>();
                    centroDto.put("id", centerId);

                    // Obtener información del centro médico
                    String nombreCentro = "Centro Médico ID: " + centerId;
                    String direccion = "No disponible";
                    try {
                        ResponseEntity<MedicalCenterRead> centerResponse = centerServiceClient.getOne(
                                centerId, false, MedicalCenterServiceClient.ROLE);
                        if (centerResponse.getBody() != null) {
                            MedicalCenterRead centro = centerResponse.getBody();
                            nombreCentro = centro.name();
                            direccion = centro.address();
                        }
                    } catch (Exception e) {
                        log.warn("Error al obtener información del centro médico ID: {}", centerId, e);
                    }

                    centroDto.put("nombreCentro", nombreCentro);
                    centroDto.put("direccion", direccion);
                    centroDto.put("consultas", new ArrayList<Map<String, Object>>());

                    centroMap.put(centerId, centroDto);
                }

                // Agregar detalle de consulta
                Map<String, Object> detalleConsulta = new HashMap<>();
                detalleConsulta.put("consultaId", consulta.getId());

                // Obtener información del médico
                String especialidad = "No disponible";
                String nombreMedico = "Médico ID: " + consulta.getDoctorId();
                try {
                    ResponseEntity<DoctorRead> doctorResponse = doctorServiceClient.getOne(
                            consulta.getDoctorId(), false, DoctorServiceClient.ROLE);
                    if (doctorResponse.getBody() != null) {
                        DoctorRead doctor = doctorResponse.getBody();
                        nombreMedico = "Doctor " + doctor.userId();
                        if (doctor.specialtyName() != null) {
                            especialidad = doctor.specialtyName();
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error al obtener información del médico ID: {}", consulta.getDoctorId(), e);
                }

                // Obtener información del paciente
                String nombrePaciente = patientRepository.findById(consulta.getPatientId())
                        .map(p -> p.getFirstName() + " " + p.getLastName())
                        .orElse("Paciente ID: " + consulta.getPatientId());

                detalleConsulta.put("nombreMedico", nombreMedico);
                detalleConsulta.put("especialidad", especialidad);
                detalleConsulta.put("nombrePaciente", nombrePaciente);
                detalleConsulta.put("fechaConsulta", consulta.getDate());
                detalleConsulta.put("estado", consulta.getDeleted() ? "CANCELADA" : "ACTIVA");

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> consultasList = (List<Map<String, Object>>) centroMap.get(centerId).get("consultas");
                consultasList.add(detalleConsulta);
            }

            // Aplicar paginación manual
            List<Map<String, Object>> resultado = new ArrayList<>(centroMap.values());

            int total = resultado.size();
            int pagina = request.getPagina() != null ? request.getPagina() : 0;
            int tamanio = request.getTamanio() != null ? request.getTamanio() : 20;
            int inicio = pagina * tamanio;
            int fin = Math.min(inicio + tamanio, total);

            if (inicio < total) {
                resultado = resultado.subList(inicio, fin);
            } else {
                resultado = new ArrayList<>();
            }

            log.info("Consultas por centro médico obtenidas con éxito: {} centros", resultado.size());
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener consultas por centro médico", e);
            throw e;
        }
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @PostMapping("/by-month")
    public ResponseEntity<List<Map<String, Object>>> getConsultasByMonth(
            @RequestBody MonthlyReportRequestDTO request,
            @PageableDefault(sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Recibida solicitud para obtener consultas mensuales: {}", request);
        try {
            // Convertir fechas
            LocalDateTime fechaInicioDateTime = request.getFechaInicio() != null ? request.getFechaInicio().atStartOfDay() : null;
            LocalDateTime fechaFinDateTime = request.getFechaFin() != null ? request.getFechaFin().atTime(LocalTime.MAX) : null;
            Boolean estadoBoolean = convertirEstado(request.getEstado());

            // Obtener todas las consultas para agrupar por mes usando Specifications
            Specification<MedicalConsultation> spec = MedicalConsultationSpecifications.withFilters(
                    fechaInicioDateTime,
                    fechaFinDateTime,
                    estadoBoolean,
                    request.getCentrosMedicos(),
                    request.getMedicos()
            );
            List<MedicalConsultation> consultas = consultationsRepository.findAll(spec, Pageable.unpaged()).getContent();

            // Agrupar consultas por mes
            Map<String, Map<String, Object>> mesMap = new HashMap<>();

            for (MedicalConsultation consulta : consultas) {
                LocalDateTime date = consulta.getDate();
                int month = date.getMonthValue();
                int year = date.getYear();
                String key = year + "-" + month;

                if (!mesMap.containsKey(key)) {
                    Map<String, Object> mesDto = new HashMap<>();
                    mesDto.put("mes", month);
                    mesDto.put("anio", year);
                    mesDto.put("totalConsultas", 0);
                    mesDto.put("especialidades", new ArrayList<Map<String, Object>>());

                    mesMap.put(key, mesDto);
                }

                // Incrementar contador de consultas
                int totalConsultas = (int) mesMap.get(key).get("totalConsultas") + 1;
                mesMap.get(key).put("totalConsultas", totalConsultas);

                // Actualizar contador por especialidad
                String especialidad = "No disponible";
                try {
                    ResponseEntity<DoctorRead> doctorResponse = doctorServiceClient.getOne(
                            consulta.getDoctorId(), false, DoctorServiceClient.ROLE);
                    if (doctorResponse.getBody() != null && doctorResponse.getBody().specialtyName() != null) {
                        especialidad = doctorResponse.getBody().specialtyName();
                    }
                } catch (Exception e) {
                    log.warn("Error al obtener especialidad del médico ID: {}", consulta.getDoctorId(), e);
                }

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> especialidadesList = (List<Map<String, Object>>) mesMap.get(key).get("especialidades");

                boolean found = false;
                for (Map<String, Object> esp : especialidadesList) {
                    if (esp.get("nombreEspecialidad").equals(especialidad)) {
                        esp.put("cantidadConsultas", (int) esp.get("cantidadConsultas") + 1);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Map<String, Object> resumenEsp = new HashMap<>();
                    resumenEsp.put("nombreEspecialidad", especialidad);
                    resumenEsp.put("cantidadConsultas", 1);
                    especialidadesList.add(resumenEsp);
                }
            }

            // Convertir a lista y ordenar por año y mes
            List<Map<String, Object>> resultado = new ArrayList<>(mesMap.values());
            resultado.sort((a, b) -> {
                int yearA = (int) a.get("anio");
                int yearB = (int) b.get("anio");
                if (yearA != yearB) {
                    return Integer.compare(yearA, yearB);
                } else {
                    return Integer.compare((int) a.get("mes"), (int) b.get("mes"));
                }
            });

            // Aplicar paginación manual
            int total = resultado.size();
            int pagina = request.getPagina() != null ? request.getPagina() : 0;
            int tamanio = request.getTamanio() != null ? request.getTamanio() : 20;
            int inicio = pagina * tamanio;
            int fin = Math.min(inicio + tamanio, total);

            if (inicio < total) {
                resultado = resultado.subList(inicio, fin);
            } else {
                resultado = new ArrayList<>();
            }

            log.info("Consultas mensuales obtenidas con éxito: {} meses", resultado.size());
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener consultas mensuales", e);
            throw e;
        }
    }

    /**
     * Convierte un string de estado a un booleano para filtrado
     */
    private Boolean convertirEstado(String estado) {
        if (estado == null || estado.isEmpty()) {
            return null;
        }

        return switch (estado.toUpperCase()) {
            case "CANCELADA" -> true;
            case "ACTIVA" -> false;
            default -> null;
        };
    }
}