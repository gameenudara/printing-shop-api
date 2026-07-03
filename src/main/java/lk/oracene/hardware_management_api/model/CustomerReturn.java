package lk.oracene.hardware_management_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_returns", indexes = {
        @Index(name = "idx_customer_return_return_date", columnList = "return_date")
})
@Data
@EqualsAndHashCode(callSuper = false)
public class CustomerReturn extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long returnId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sales sale;

    private LocalDateTime returnDate;

    private String note;
}
