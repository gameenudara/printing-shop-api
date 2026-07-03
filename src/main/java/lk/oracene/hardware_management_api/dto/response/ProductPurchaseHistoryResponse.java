package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ProductPurchaseHistoryResponse {

    private String billNumber;
    private LocalDate billDate;
    private String supplierName;
    private Integer quantity;
    private BigDecimal unitCostPrice;
}
