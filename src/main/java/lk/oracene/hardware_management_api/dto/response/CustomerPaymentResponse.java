package lk.oracene.hardware_management_api.dto.response;

import lk.oracene.hardware_management_api.model.PaymentMethod;
import lk.oracene.hardware_management_api.model.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CustomerPaymentResponse {

    private Long paymentId;
    private Long customerId;
    private String customerName;
    private Long saleId;
    private BigDecimal paidAmount;
    private LocalDateTime paidAt;
    private PaymentMethod method;
    private PaymentStatus status;
    private String referenceNo;
    private Long chequeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
