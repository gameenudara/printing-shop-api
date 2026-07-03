package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CustomerOutstandingReportResponse {

    private int totalCustomers;
    private BigDecimal totalOutstanding;
    private List<CustomerOutstandingRow> customers;

    @Data
    @Builder
    public static class CustomerOutstandingRow {
        private Long customerId;
        private String customerName;
        private String phone;
        private BigDecimal totalBillAmount;
        private BigDecimal paidAmount;
        private BigDecimal outstandingAmount;
        private LocalDateTime oldestPendingDate;
        private long daysPending;
    }
}
