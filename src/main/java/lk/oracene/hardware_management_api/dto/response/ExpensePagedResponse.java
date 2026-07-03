package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ExpensePagedResponse {

    private long totalExpenseCount;
    private BigDecimal totalExpenseAmount;

    private List<ExpenseResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
