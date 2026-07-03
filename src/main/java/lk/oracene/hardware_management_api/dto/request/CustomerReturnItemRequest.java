package lk.oracene.hardware_management_api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lk.oracene.hardware_management_api.model.ReturnCondition;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerReturnItemRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Sale item ID is required")
    private Long saleItemId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    @NotNull(message = "Condition is required")
    private ReturnCondition condition;
}
