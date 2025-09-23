package com.hospital.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.hospital.enums.GenderType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private GenderType gender;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("center_id")
    private Long centerId;


}
