package lk.oracene.hardware_management_api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SupplierReturnRequest {

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    private Long supplierBillId;

    private String note;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<SupplierReturnItemRequest> items;
}
