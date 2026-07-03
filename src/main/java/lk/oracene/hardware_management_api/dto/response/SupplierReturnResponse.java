package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SupplierReturnResponse {

    private Long supplierReturnId;
    private Long supplierId;
    private String supplierName;
    private LocalDateTime returnDate;
    private BigDecimal totalReturnAmount;
    private String note;
    private SupplierBillSummaryResponse bill;
    private List<SupplierReturnItemResponse> items;
    private LocalDateTime createdAt;
    private String createdBy;
}
