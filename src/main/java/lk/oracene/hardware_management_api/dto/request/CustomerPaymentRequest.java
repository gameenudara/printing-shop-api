package lk.oracene.hardware_management_api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lk.oracene.hardware_management_api.model.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerPaymentRequest {

    private Long customerId;

    @NotNull(message = "Sale ID is required")
    private Long saleId;

    @NotNull(message = "Paid amount is required")
    @DecimalMin(value = "0.01", message = "Paid amount must be greater than zero")
    private BigDecimal paidAmount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;

    private String referenceNo;
}
