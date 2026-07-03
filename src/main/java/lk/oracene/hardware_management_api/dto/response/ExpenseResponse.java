package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ExpenseResponse {

    private Long expenseId;
    private String title;
    private BigDecimal amount;
    private LocalDateTime expenseDate;
    private String note;
}
