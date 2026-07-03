package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_items")
@Data
@EqualsAndHashCode(callSuper = false)
public class SalesItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long saleItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id")
    private Sales sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountPct;
    private BigDecimal flatDiscount;
    private BigDecimal lineTotal;
}
