package consulting_service.feign.auth_service.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;
import consulting_service.enums.GenderType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseSV {

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

    @JsonProperty("center_name")
    private String centerName;



}