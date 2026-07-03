package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProfitLossResponse {

    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private BigDecimal revenue;
    private BigDecimal costOfRevenue;
    private BigDecimal grossProfit;
    private BigDecimal expenses;
    private BigDecimal netProfit;
    private String type;
    private BigDecimal profitMargin;
}
