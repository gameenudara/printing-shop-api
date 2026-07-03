package lk.oracene.hardware_management_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lk.oracene.hardware_management_api.model.RoleType;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number")
    private String phone;

    @NotNull
    private RoleType role;
}
