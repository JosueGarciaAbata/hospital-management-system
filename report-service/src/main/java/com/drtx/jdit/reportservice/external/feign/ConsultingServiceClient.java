package com.drtx.jdit.reportservice.external.feign;

import com.drtx.jdit.reportservice.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Feign client to communicate with the consulting service
 */
@FeignClient(name = "consulting-service", configuration = {com.drtx.jdit.reportservice.config.FeignConfig.class})
public interface ConsultingServiceClient {

    String DEFAULT_ROLE = "ADMIN";

    /**
     * Gets consultations by specialty with filtering parameters via POST
     */
    @PostMapping("/api/consulting/reports/by-specialty")
    List<Map<String, Object>> getConsultationsBySpecialty(
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Roles") String roles,
            @RequestBody SpecialtyReportRequestDTO request
    );

    /**
     * Gets consultations by doctor with filtering parameters via POST
     */
    @PostMapping("/api/consulting/reports/by-doctor")
    List<Map<String, Object>> getConsultationsByDoctor(
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Roles") String roles,
            @RequestBody DoctorReportRequestDTO request
    );

    /**
     * Gets consultations by medical center with filtering parameters via POST
     */
    @PostMapping("/api/consulting/reports/by-center")
    List<Map<String, Object>> getConsultationsByCenter(
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Roles") String roles,
            @RequestBody MedicalCenterReportRequestDTO request
    );

    /**
     * Gets monthly consultations with filtering parameters via POST
     */
    @PostMapping("/api/consulting/reports/by-month")
    List<Map<String, Object>> getConsultationsByMonth(
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Roles") String roles,
            @RequestBody MonthlyReportRequestDTO request
    );

}