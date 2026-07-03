package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class RevenueTrendResponse {

    private BigDecimal totalRevenue;
    private BigDecimal averageRevenue;
    private BigDecimal highestRevenue;
    private BigDecimal lowestRevenue;
    private List<String> labels;
    private List<BigDecimal> values;
}
