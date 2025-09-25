package com.hospital.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MedicalCenterDto {
    private Long id;
    private String name;
    private String city;
    private String address;
}
