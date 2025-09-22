package com.hospital.admin_service.external.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.Set;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CreateUserForDoctorRequest(
        String username,
        String password,
        String gender,
        String firstName,
        String lastName,
        Long centerId,
        Set<RoleRequest> roles
) {}