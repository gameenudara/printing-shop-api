package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class FinancialSummaryResponse {

    private LocalDate date;
    private BigDecimal revenue;
    private BigDecimal expenses;
    private BigDecimal netProfit;
}
