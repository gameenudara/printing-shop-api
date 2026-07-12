package lk.oracene.hardware_management_api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CloseDrawerRequest {

    @NotNull(message = "Counted closing balance is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Closing balance cannot be negative")
    private BigDecimal closingBalanceActual;

    private String notes;
}
