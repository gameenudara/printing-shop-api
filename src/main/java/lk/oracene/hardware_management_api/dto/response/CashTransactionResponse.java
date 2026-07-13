package lk.oracene.hardware_management_api.dto.response;

import lk.oracene.hardware_management_api.model.CashTransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CashTransactionResponse {

    private Long transactionId;
    private CashTransactionType type;
    private BigDecimal amount;
    private String reason;
    private Long referenceId;
    private LocalDateTime createdAt;
    private String createdBy;
}
