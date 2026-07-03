package lk.oracene.hardware_management_api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lk.oracene.hardware_management_api.model.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DraftConfirmRequest {

    @DecimalMin(value = "0.01", message = "Received amount must be greater than zero")
    private BigDecimal receivedAmount;

    private PaymentMethod paymentMethod;

    private String paymentReferenceNo;
}
