package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Table(name = "supplier_bill_items")
@Data
@EqualsAndHashCode(callSuper = false)
public class SupplierBillItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long supplierBillItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_bill_id")
    private SupplierBill supplierBill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;
    private BigDecimal unitCostPrice;
}
