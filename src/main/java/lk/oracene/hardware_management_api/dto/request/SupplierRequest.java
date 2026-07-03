package lk.oracene.hardware_management_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplierRequest {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 100, message = "Supplier name must not exceed 100 characters")
    private String name;

    @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$", message = "Invalid phone number")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 255)
    private String address;
}
