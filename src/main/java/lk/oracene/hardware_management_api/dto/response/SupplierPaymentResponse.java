package lk.oracene.hardware_management_api.dto.response;

import lk.oracene.hardware_management_api.model.SupplierPaymentMethod;
import lk.oracene.hardware_management_api.model.SupplierPaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SupplierPaymentResponse {

    private Long supplierPaymentId;
    private Long supplierBillId;
    private String billNumber;
    private BigDecimal amount;
    private SupplierPaymentMethod method;
    private SupplierPaymentStatus status;
    private String referenceNo;
    private LocalDateTime paidAt;
    private Long chequeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
