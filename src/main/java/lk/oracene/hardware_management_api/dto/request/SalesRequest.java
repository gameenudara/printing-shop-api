package lk.oracene.hardware_management_api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import lk.oracene.hardware_management_api.model.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SalesRequest {

    private Long customerId;

    @DecimalMin(value = "0.00", inclusive = true, message = "Discount amount cannot be negative")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", inclusive = true, message = "Tax amount cannot be negative")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<SalesItemRequest> items;

    @DecimalMin(value = "0.01", message = "Received amount must be greater than zero")
    private BigDecimal receivedAmount;

    private PaymentMethod paymentMethod;

    private String paymentReferenceNo;
}
