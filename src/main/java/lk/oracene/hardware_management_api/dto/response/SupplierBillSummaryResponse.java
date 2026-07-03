package lk.oracene.hardware_management_api.dto.response;

import lk.oracene.hardware_management_api.model.SupplierBillStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class SupplierBillSummaryResponse {

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
}
