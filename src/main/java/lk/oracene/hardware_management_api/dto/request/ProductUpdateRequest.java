package lk.oracene.hardware_management_api.dto.request;

import jakarta.validation.constraints.*;
import lk.oracene.hardware_management_api.model.Unit;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Product name is required")
    @Size(max = 150, message = "Product name must not exceed 150 characters")
    private String name;

    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;

    @Size(max = 50, message = "Size must not exceed 50 characters")
    private String size;

    @Size(max = 50, message = "Colour must not exceed 50 characters")
    private String colour;

    @Size(max = 500)
    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Cost price cannot be negative")
    private BigDecimal costPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Unit price cannot be negative")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Discount cannot be negative")
    @DecimalMax(value = "100.0", inclusive = true, message = "Discount cannot exceed 100%")
    private BigDecimal discount;

    @NotNull(message = "Unit is required")
    private Unit unit;
}
