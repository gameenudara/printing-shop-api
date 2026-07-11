package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales", indexes = {
        @Index(name = "idx_sales_sale_date", columnList = "sale_date"),
        @Index(name = "idx_sales_status", columnList = "status"),
        @Index(name = "idx_sales_sale_date_status", columnList = "sale_date, status")
})
@Data
@EqualsAndHashCode(callSuper = false)
public class Sales extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long salesId;

    @Column(unique = true)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime saleDate;
    private BigDecimal subTotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private SalesStatus status;

    private String cashier;
}
