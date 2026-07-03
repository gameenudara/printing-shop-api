package lk.oracene.hardware_management_api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryAvailabilityResponse {
    private Long categoryId;
    private String categoryName;
    private Boolean isActive;
    private Boolean hasProducts;
    private String message;
}
