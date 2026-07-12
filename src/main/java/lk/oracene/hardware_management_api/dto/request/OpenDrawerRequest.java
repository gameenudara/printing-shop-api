package lk.oracene.hardware_management_api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OpenDrawerRequest {

    @NotNull(message = "Opening balance is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Opening balance cannot be negative")
    private BigDecimal openingBalance;

    private String notes;
}
