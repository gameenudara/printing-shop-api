package lk.oracene.hardware_management_api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SupplierBillItemUpdateRequest {

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @DecimalMin(value = "0.01", message = "Unit cost price must be greater than zero")
    private BigDecimal unitCostPrice;

    private Boolean isReturn;

    @DecimalMin(value = "0.0", inclusive = true, message = "Unit price cannot be negative")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Discount cannot be negative")
    private BigDecimal discount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Supplier discount cannot be negative")
    private BigDecimal supplierDiscount;
}
