package lk.oracene.hardware_management_api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CashDrawerSessionResponse {

    private Long sessionId;
    private BigDecimal openingBalance;
    private BigDecimal currentBalance;
    private BigDecimal totalCashIn;
    private BigDecimal totalCashOut;
    private String notes;
    private String openedBy;
    private LocalDateTime openedAt;
    private List<CashTransactionResponse> transactions;
}
