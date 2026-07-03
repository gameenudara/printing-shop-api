package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class TopSellingProductsResponse {

    private List<String> labels;
    private List<BigDecimal> quantities;
    private List<BigDecimal> revenues;
}
