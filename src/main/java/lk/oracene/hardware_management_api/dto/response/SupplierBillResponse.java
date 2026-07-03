package lk.oracene.hardware_management_api.dto.response;

import lk.oracene.hardware_management_api.model.SupplierBillStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SupplierBillResponse {

    private Long supplierBillId;
    private Long supplierId;
    private String supplierName;
    private String billNumber;
    private LocalDate billDate;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private BigDecimal totalReturnAmount;
    private SupplierBillStatus status;
    private String notes;
    private List<SupplierBillItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
