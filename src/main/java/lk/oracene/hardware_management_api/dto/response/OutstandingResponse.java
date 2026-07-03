package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OutstandingResponse {

    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
}
