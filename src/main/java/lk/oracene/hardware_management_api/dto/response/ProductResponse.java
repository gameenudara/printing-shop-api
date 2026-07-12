package lk.oracene.hardware_management_api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lk.oracene.hardware_management_api.model.Unit;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {
    private Long productId;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String brand;
    private String size;
    private String colour;
    private String description;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private Unit unit;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
