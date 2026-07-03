package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ExpenseDateRangeResponse {

    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private long totalExpenseCount;
    private BigDecimal totalExpenseAmount;
    private List<ExpenseResponse> expenses;
}
