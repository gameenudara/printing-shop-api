package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Table(name = "customer_return_items")
@Data
@EqualsAndHashCode(callSuper = false)
public class CustomerReturnItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long returnItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private CustomerReturn customerReturn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_item_id", nullable = false)
    private SalesItem saleItem;

    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_condition")
    private ReturnCondition condition;

    private BigDecimal refundAmount;
}
