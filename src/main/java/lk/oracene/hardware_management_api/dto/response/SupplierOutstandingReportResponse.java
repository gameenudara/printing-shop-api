package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SupplierOutstandingReportResponse {

    private int totalSuppliers;
    private BigDecimal totalOutstanding;
    private List<SupplierOutstandingRow> suppliers;

    @Data
    @Builder
    public static class SupplierOutstandingRow {
        private Long supplierId;
        private String supplierName;
        private String phone;
        private BigDecimal totalBillAmount;
        private BigDecimal paidAmount;
        private BigDecimal totalReturnAmount;
        private BigDecimal outstandingAmount;
        private LocalDate oldestOutstandingBillDate;
        private long daysPending;
    }
}
