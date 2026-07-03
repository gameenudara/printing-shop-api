package lk.oracene.hardware_management_api.dto.response;

import lk.oracene.hardware_management_api.model.ReturnCondition;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CustomerReturnItemResponse {

    private Long returnItemId;
    private Long productId;
    private String productName;
    private Long saleItemId;
    private BigDecimal quantity;
    private ReturnCondition condition;
    private BigDecimal refundAmount;
}
