package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SalesReportResponse {

    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    private long totalSalesCount;
    private BigDecimal totalRevenue;
    private BigDecimal totalDiscountAmount;
    private BigDecimal totalTaxAmount;

    private List<SalesResponse> sales;
}
