package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Table(name = "supplier_return_items")
@Data
@EqualsAndHashCode(callSuper = false)
public class SupplierReturnItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long supplierReturnItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_return_id", nullable = false)
    private SupplierReturn supplierReturn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private BigDecimal quantity;

    private BigDecimal unitCostPrice;
}
