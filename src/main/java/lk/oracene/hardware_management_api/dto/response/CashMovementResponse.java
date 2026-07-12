package lk.oracene.hardware_management_api.dto.response;

import lk.oracene.hardware_management_api.model.CashMovementType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CashMovementResponse {

    private Long movementId;
    private CashMovementType type;
    private BigDecimal amount;
    private String reason;
    private Long referenceId;
    private LocalDateTime createdAt;
    private String createdBy;
}
