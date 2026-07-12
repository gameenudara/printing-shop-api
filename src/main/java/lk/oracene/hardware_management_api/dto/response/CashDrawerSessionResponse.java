package lk.oracene.hardware_management_api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lk.oracene.hardware_management_api.model.CashDrawerStatus;
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
    private CashDrawerStatus status;
    private BigDecimal openingBalance;
    private BigDecimal currentBalance;
    private BigDecimal closingBalanceActual;
    private BigDecimal expectedBalance;
    private BigDecimal variance;
    private String notes;
    private String openedBy;
    private LocalDateTime openedAt;
    private String closedBy;
    private LocalDateTime closedAt;
    private List<CashMovementResponse> movements;
}
