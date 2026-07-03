package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_sku", columnList = "sku"),
        @Index(name = "idx_product_barcode", columnList = "barcode"),
        @Index(name = "idx_product_is_active", columnList = "is_active"),
        @Index(name = "idx_product_name_brand_size_colour", columnList = "name, brand, size, colour"),
        @Index(name = "idx_product_stock_quantity", columnList = "stock_quantity")
})
@Data
@EqualsAndHashCode(callSuper = false)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(length = 150)
    private String name;

    @Column(length = 100)
    private String brand;

    @Column(length = 50)
    private String size;

    @Column(length = 50)
    private String colour;
    private String sku;
    private String barcode;
    private String description;

    @Column(precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 12, scale = 3)
    private BigDecimal stockQuantity = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal discount;

    @Column(precision = 5, scale = 2)
    private BigDecimal supplierDiscount;

    private Integer reorderLevel;

    @Enumerated(EnumType.STRING)
    private Unit unit;

    private Boolean isActive = true;

    private Boolean isReturn = true;
}
