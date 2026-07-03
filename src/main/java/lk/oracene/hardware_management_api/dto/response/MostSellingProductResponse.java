package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MostSellingProductResponse {

    private Long productId;
    private String productName;
    private BigDecimal totalQuantitySold;
    private BigDecimal totalRevenue;
}
