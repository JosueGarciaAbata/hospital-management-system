package consulting_service.services.reports;

import consulting_service.dtos.response.reports.PaginationInfoDTO;
import consulting_service.entities.MedicalConsultation;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de utilidades para reportes
 */
@Service
public class ReportUtilsService {

    /**
     * Convierte una fecha a inicio del día (00:00:00)
     */
    public LocalDateTime toStartOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    /**
     * Convierte una fecha a fin del día (23:59:59)
     */
    public LocalDateTime toEndOfDay(LocalDate date) {
        return date != null ? date.atTime(LocalTime.MAX) : null;
    }

    /**
     * Construye información de paginación para reportes
     */
    public PaginationInfoDTO buildPaginationInfo(Page<?> page) {
        return PaginationInfoDTO.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    /**
     * Construye el mapa de distribución semanal de consultas
     */
    public Map<String, Integer> buildWeeklyDistribution(List<MedicalConsultation> consultations) {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        // Inicializa todos los días con cero
        for (String day : days) {
            distribution.put(day, 0);
        }

        // Contabiliza consultas por día de la semana
        for (MedicalConsultation consultation : consultations) {
            String dayName = consultation.getDate().getDayOfWeek().toString();
            // Formatea primera letra mayúscula, resto minúscula (Monday, Tuesday, etc.)
            String formattedDay = dayName.charAt(0) + dayName.substring(1).toLowerCase();
            distribution.merge(formattedDay, 1, Integer::sum);
        }

        return distribution;
    }

    /**
     * Analiza el patrón de estacionalidad en consultas médicas
     */
    public String analyzeSeasonality(List<MedicalConsultation> consultations) {
        Map<Integer, Long> perMonth = consultations.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        cons -> cons.getDate().getMonthValue(),
                        java.util.stream.Collectors.counting()
                ));

        if (perMonth.size() < 3) {
            return "DATOS_INSUFICIENTES";
        }

        long max = perMonth.values().stream().mapToLong(v -> v).max().orElse(0);
        long min = perMonth.values().stream().mapToLong(v -> v).min().orElse(0);

        // Si la diferencia entre el mes con más consultas y el de menos
        // es superior al 30% del total, hay estacionalidad
        return (max - min > consultations.size() * 0.3) ?
               "VARIACIONES_ESTACIONALES" :
               "PATRÓN_ESTABLE";
    }
}
