package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TodaySummaryResponse {

    private Long totalOrders;
    private BigDecimal totalSales;
    private BigDecimal totalMoney;
}
